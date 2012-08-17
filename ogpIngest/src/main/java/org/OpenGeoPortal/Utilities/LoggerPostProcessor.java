package org.OpenGeoPortal.Utilities;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

/** LoggerPostProcessor => Custom Spring BeanPostProcessor 
 * 
 * http://jgeeks.blogspot.com/2008/10/auto-injection-of-logger-into-spring.html
 * 
 **/

public class LoggerPostProcessor implements BeanPostProcessor {

   public Object postProcessAfterInitialization(Object bean, String beanName) throws

       BeansException {

       return bean;

   }



   public Object postProcessBeforeInitialization(final Object bean, String beanName)

         throws BeansException {

       ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {

               //@SuppressWarnings("unchecked")

               public void doWith(Field field) throws IllegalArgumentException,

                   IllegalAccessException {

                   ReflectionUtils.makeAccessible(field);

                   //Check if the field is annotated with @OgpLogger

                   if (field.getAnnotation(OgpLogger.class) != null) {

                       Logger logger = LoggerFactory.getLogger(bean.getClass());

                       field.set(bean, logger);

                   }

               }

       });



       return bean;

   }

}


