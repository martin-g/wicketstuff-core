package org.wicketstuff.shiro.authz;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.wicketstuff.shiro.annotation.AnnotationsShiroAuthorizationStrategy;
import org.wicketstuff.shiro.annotation.ShiroSecurityConstraint;

public class ShiroUnauthorizedComponentListener implements IUnauthorizedComponentInstantiationListener 
{
  private final Class<? extends Page> loginPage;
  private final Class<? extends Page> unauthorizedPage;
  private AnnotationsShiroAuthorizationStrategy annotationStrategy = null;
  
  public ShiroUnauthorizedComponentListener( 
      Class<? extends Page> loginPage, 
      Class<? extends Page> unauthorizedPage, 
      AnnotationsShiroAuthorizationStrategy s  ) 
  {
    this.loginPage = loginPage;
    this.unauthorizedPage = unauthorizedPage;
    annotationStrategy = s;
  }

  public void onUnauthorizedInstantiation(Component component) 
  {
    Subject subject = SecurityUtils.getSubject();
    boolean notLoggedIn = (subject.getPrincipal()==null);
    Class<? extends Page> page = notLoggedIn ? loginPage : unauthorizedPage;
    
    if( annotationStrategy != null ) {
      ShiroSecurityConstraint fail = annotationStrategy.checkInvalidInstantiation(  component.getClass() );
      if( fail != null ) {
        if( notLoggedIn ) {
          addLoginMessagesAndGetPage(fail, component, page);
        }
        else {
          addUnauthorizedMessagesAndGetPage(fail, component, page);
        }
      }
    }
    
    if( notLoggedIn ) {
      // the login page
      throw new RestartResponseAtInterceptPageException( page );
    }
    // the unauthorized page
    throw new RestartResponseException( page );
  }
  

  public AnnotationsShiroAuthorizationStrategy getAnnotationStrategy() {
    return annotationStrategy;
  }

  public void setAnnotationStrategy(
      AnnotationsShiroAuthorizationStrategy annotationStrategy) {
    this.annotationStrategy = annotationStrategy;
  }

  //----------------------------------------------------------------------------
  //----------------------------------------------------------------------------
  
  protected Class<? extends Page> addUnauthorizedMessagesAndGetPage( 
      ShiroSecurityConstraint constraint, 
      Component component, 
      Class<? extends Page> page )
  {
    if( constraint.unauthorizedMessage().length() > 0 ) {
      Session.get().info( getMessage(constraint.unauthorizedMessage(), constraint, component ) );
    }
    if( constraint.unauthorizedPage() != Page.class ) {
      page = constraint.unauthorizedPage();
    }
    return page;
  }

  protected Class<? extends Page> addLoginMessagesAndGetPage( 
      ShiroSecurityConstraint constraint, 
      Component component, 
      Class<? extends Page> page )
  {   
    if( constraint.loginMessage().length() > 0 ) {
      Session.get().info( getMessage(constraint.loginMessage(), constraint, component ) );
    }
    if( constraint.loginPage() != Page.class ) {
      page = constraint.loginPage();
    }
    return page;
  }
  
  
  protected String getMessage( String key, ShiroSecurityConstraint anno, Component comp )
  {
    return key; // TODO, this could be more complicated....
  }
}
