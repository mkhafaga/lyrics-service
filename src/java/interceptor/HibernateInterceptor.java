/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interceptor;

import daos.HibernateUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

/**
 *
 * @author Khafaga
 */
public class HibernateInterceptor implements Filter {

//    private Session currentSession;
    private SessionFactory sf;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        currentSession = HibernateUtil.getCurrentSession();
        sf =  HibernateUtil.getSessionFactory();
        System.out.println("Does this filter execute?");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            sf.getCurrentSession().beginTransaction();
            //  Transaction tx =   currentSession.beginTransaction();
            chain.doFilter(request, response);
            //  tx.commit();
            sf.getCurrentSession().getTransaction().commit();
           

        } catch (StaleObjectStateException staleEx) {
            System.out.println("This interceptor does not implement optimistic concurrency control!");
            System.out.println("Your application will not work until you add compensation actions!");
            // Rollback, close everything, possibly compensate for any permanent changes  
            // during the conversation, and finally restart business conversation. Maybe  
            // give the user of the application a chance to merge some of his work with  
            // fresh data... what you do here depends on your applications design.  
            throw staleEx;
        } catch (Throwable ex) {
            // Rollback only  
            ex.printStackTrace();
            try {
                if (sf.getCurrentSession().getTransaction().isActive()) {
                    System.out.println("Trying to rollback database transaction after exception");
                    sf.getCurrentSession().getTransaction().rollback();
                }
            } catch (Throwable rbEx) {
                System.out.println("Could not rollback transaction after exception!");
            }

            // Let others handle it... maybe another interceptor for exceptions?  
            throw new ServletException(ex);
        }
    }

    @Override
    public void destroy() {
    }

}
