SPK-Packager Maven Plugin for Synology Diskstations
========
This Maven Plugin can be used to pack .SPK-Files from runnable JARs to neatly deploy them to your Synology Diskstation via Package Manager. I developed this with Spring-Boot Applications in mind (with or without Embedded Tomcats), but should work for other stuff as well.

Prerequisites
--------
1. A Synology Diskstation with a JRE installed (via the "Java Manager" Package). Note that the DSM V 6.0 has Java 8 - restrain from fancy lambda stuff if you want to deploy to earlier DSMs.
2. Maven 2 or later on your dev-box.
3. Some Java app you want to deploy, which you have Maven-controlled sources for.

Install
--------
//TODO: put to some repo, when project is a bit more mature...

    > curl "https://codeload.github.com/lost-carrier/spk-packager-plugin/zip/v0.1.3" -o spk-packager-plugin-0.1.3.zip
    > unzip spk-packager-plugin-0.1.3.zip
    > cd spk-packager-plugin-0.1.3
...and...

    > mvn clean install

Usage
--------
Add the plugin to your pom.xml:

    <plugins>
    [...]
	  <plugin>
		<groupId>com.losty.maven.synology</groupId>
		<artifactId>spk-packager</artifactId>
		<version>0.1.3</version>
	  </plugin>
    </plugins>
...and compile your project like this:

     > mvn clean package spk-packager:package

You will see something like this, when everything went fine:

    [...]
    [INFO] --- spk-packager:0.1.3:package (default-cli) @ my-project ---
    [INFO] Successfully packed /path/to/workspace/my-project/target/my-project-1.0.0.spk
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [...]
Next, log into your Synology Diskstation DSM, go to "Package Manager", hit "Install Manually" and upload your "my-project-1.0.0.spk" - e voila! You'll find your project in the list of the installed packages.

What else?
--------
There's a lot of stuff that can be configured for a SPK-File.

	<configuration>
		<java>/var/packages/Java8/target/j2sdk-image/bin/java</java>    <!-- Where to find Java on target box -->
		<jvmArgs>-Dserver.port=7071</jvmArgs>    <!-- Arguments to be passed to the JVM -->
		<progArgs>--logging.config=./logback.xml</progArgs>    <!-- Arguments to your App -->
		<distributor>me</distributor>    <!-- just meta stuff -->
		<distributorUrl>http://www.losty.ch</distributorUrl>    <!-- just meta stuff -->
		<maintainer>also-me</maintainer>    <!-- just meta stuff -->
		<maintainerUrl>http://www.losty.ch</maintainerUrl>    <!-- just meta stuff -->
		<minDsmVersion>5.0</minDsmVersion>    <!-- Minimum DSM version required -->
		<reportUrl>https://github.com/you/your-project/issues</reportUrl>    <!-- A "Beta" flag will appear together with a "Feedback" button -->
		<addFiles>
			<property>
				<name>src/main/resources/application-spk.properties</name>    <!-- Relative to your project basedir -->
				<value>application.properties</value>    <!-- location in the package -->
			</property>
			<property>
				<name>src/main/resources/logback-spk.xml</name>
				<value>logback.xml</value>
			</property>
		</addFiles>
	</configuration>
However: all those tags are optional.
 
Important
--------
If you want to deploy some Web App (e.g. with Embedded Tomcat) and route requests thru the Apache (...or "Web Station" in Synology terms), you can place a .htaccess files to some directory or virtual host, which proxies all requests to your application. E.g.:
    
    RewriteEngine On
    RewriteRule ^/?(.*)$ http://localhost:7071/$1 [P]
If your app listens to port 7071.

The tricky part is, that the Embedded Tomcat will now think it serves at http://localhost:7071 and Spring MVC will create redirects pointing "fully-qualified" to the nirvana somewhere on the box of the enduser. To deal with this, I add something like this to the (Spring Boot-)configuration:

    @Value("${proxy.name:}")
    private String proxyName;

    @Value("${proxy.port:80}")
    private Integer proxyPort;
    
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        if (proxyName != null && !proxyName.isEmpty()) {
	        TomcatConnectorCustomizer tomcatConnectorCustomizer = new TomcatConnectorCustomizer() {
	            @Override
	            public void customize(Connector connector) {
	            	LOG.info("Setting proxy to {}", proxyName);
	            	connector.setProxyName(proxyName);
	            	connector.setProxyPort(proxyPort);
	            }
	        };
	        tomcat.addConnectorCustomizers(tomcatConnectorCustomizer);
        }
	    return tomcat;
    }
   
...and set the proxy.name the DNS of the Diskstation or the VHost.

Known Issues
--------
Somehow Spring-Boot appears not to pick up the "server.port" from any application.properties. Use 
    
    <jvmArgs>--Dserver.port=7071</jvmArgs>
in the pom.xml for now.

If other stuff breaks, it might be useful to log onto the Diskstation via SSH and check...
    
    > cd /var/packages/my-project/target
...have a look around...
    
    > ls -la
    [...]
    > ls -la var
    [...]
    > ls -la var/log
    [...]
  ...or even try something totally freaky like...
    
    > java -jar application.jar
   ...and see what happens.

License
--------
I'll pick one later - don't mess with it...
However: forks, PRs and stuff is welcome! This is my very first Maven Plugin, so there's probably a lot to improve...