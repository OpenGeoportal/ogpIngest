package org.OpenGeoPortal.Utilities;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;

/** Custom @OgpLogger annotation 
 * 
 * http://jgeeks.blogspot.com/2008/10/auto-injection-of-logger-into-spring.html
 * **/
 	@Retention(RUNTIME)
	@Target(FIELD)
	@Documented
	 
	public @interface OgpLogger { }
