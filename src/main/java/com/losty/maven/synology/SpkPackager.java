package com.losty.maven.synology;

import static com.losty.maven.synology.SpkHelperUtils.md5;
import static com.losty.maven.synology.SpkHelperUtils.readToString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "package")
public class SpkPackager extends AbstractMojo {

	private final static String NEWLINE = "\n";

	@Parameter(property = "project.build.directory")
	private File outputDir;

	@Parameter(defaultValue = " ")
	private String jvmArgs;

	@Parameter(defaultValue = " ")
	private String progArgs;

	@Parameter(defaultValue = "nobody")
	private String distributor;
	
	@Parameter(defaultValue = "http://")
	private String distributorUrl;

	@Parameter(defaultValue = "nobody")
	private String maintainer;
	
	@Parameter(defaultValue = "http://")
	private String maintainerUrl;

	@Parameter
	private String reportUrl;
	
	@Parameter(defaultValue = " ")
	private String arch;

	@Parameter(defaultValue = " ")
	private String minDsmVersion;

	@Parameter(defaultValue = "java>=1.6.0.27-1")
	private String installDepPackages;

	@Parameter(defaultValue = "no")
	private String reloadUi;

	@Parameter(defaultValue = " ")
	private String changelog;
	
	@Parameter(defaultValue = "${project}")
	private MavenProject mavenProject;
	
	@Parameter
	private Properties addFiles;
	
	@Parameter
	private String packageIcon120;

	@Parameter
	private String packageIcon;

	private Templates tpl;
	
	private File generatePackageTgz() throws IOException {
		File packageFile = new File(outputDir, "package.tgz");
		File artifactFile = mavenProject.getArtifact().getFile();
		TarGz packageTarGz = null;
		try {
			packageTarGz = new TarGz(packageFile);
			EnhancedTarGz ePackageTarGz = new EnhancedTarGz(packageTarGz);
			ePackageTarGz.addFileDirect("application.jar", artifactFile.toURI().toURL());
			if (addFiles != null) {
				for (Entry<Object, Object> entry : addFiles.entrySet()) {
					File file = new File(mavenProject.getBasedir(), (String) entry.getKey());
					if (!file.exists()) {
						throw new FileNotFoundException(file.getAbsolutePath());
					}
					String target = (String) entry.getValue();
					ePackageTarGz.addFileDirect(target, file.toURI().toURL());
					getLog().info(String.format("Adding %s as %s", file, target));
				}
			}
		} finally {
			if (packageTarGz != null) {
				packageTarGz.close();
			}
		}
		return packageFile;	
	}
	
	private String generateInfoFile(String checksum) throws IOException, URISyntaxException {
		
		StringBuilder sb = new StringBuilder();
		sb.append("package=").append(mavenProject.getArtifactId()).append(NEWLINE);
		sb.append("version=").append(mavenProject.getVersion()).append(NEWLINE);
		sb.append("displayname=").append(mavenProject.getName()).append(NEWLINE);
		sb.append("description=").append(mavenProject.getDescription()).append(NEWLINE);
		
		sb.append("arch=").append(arch.trim()).append(NEWLINE);
		sb.append("firmware=").append(minDsmVersion.trim()).append(NEWLINE);
		sb.append("install_dep_packages=").append(installDepPackages.trim()).append(NEWLINE);
		sb.append("reloadui=").append(reloadUi.trim()).append(NEWLINE);
		sb.append("changelog=").append(changelog.trim()).append(NEWLINE);
		sb.append("checksum=").append(checksum).append(NEWLINE);

		sb.append("distributor=").append(distributor.trim()).append(NEWLINE);
		sb.append("distributor_url=").append(distributorUrl.trim()).append(NEWLINE);
		sb.append("maintainer=").append(maintainer.trim()).append(NEWLINE);
		sb.append("maintainer_url=").append(maintainerUrl.trim()).append(NEWLINE);
		
		if (reportUrl != null) {
			sb.append("report_url=").append(reportUrl.trim()).append(NEWLINE);
		}
		
		return sb.toString();

	}
	
	private String generateInstaller() throws IOException, URISyntaxException {
		String str = readToString(tpl.getTemplate("scripts/installer"));
		str = str.replace("##artifactId##", mavenProject.getArtifactId());
		str = str.replace("##name##", mavenProject.getName());
		return str;	
	}
	
	private String generateStartStopStatus() throws IOException, URISyntaxException {
		String str = readToString(tpl.getTemplate("scripts/start-stop-status"));
		str = str.replace("##artifactId##", mavenProject.getArtifactId());
		str = str.replace("##name##", mavenProject.getName());
		str = str.replace("##jvmArgs##", jvmArgs);
		str = str.replace("##progArgs##", progArgs);
		return str;	
	}
	
	public void execute() throws MojoExecutionException {
		
		tpl = new Templates(mavenProject.getBasedir());
		File spkFile = new File(outputDir+File.separator+mavenProject.getBuild().getFinalName()+".spk");
		TarGz spkTarGz = null;
		try {
			spkTarGz = new TarGz(spkFile);
			EnhancedTarGz eSpkTarGz = new EnhancedTarGz(spkTarGz);
			
			File packageFile = generatePackageTgz();
			String checksum = md5(packageFile);
			spkTarGz.putFile(packageFile, packageFile.getName());			
			eSpkTarGz.addTextFile("INFO", generateInfoFile(checksum));
			eSpkTarGz.addFileDirect("PACKAGE_ICON_120.PNG", tpl.getExternalOrTemplate("PACKAGE_ICON_120.PNG", packageIcon120));
			eSpkTarGz.addFileDirect("PACKAGE_ICON.PNG", tpl.getExternalOrTemplate("PACKAGE_ICON.PNG", packageIcon));
			eSpkTarGz.addFileDirect("scripts/preinst", tpl.getTemplate("scripts/preinst"), 0755);
			eSpkTarGz.addFileDirect("scripts/postinst", tpl.getTemplate("scripts/postinst"), 0755);
			eSpkTarGz.addFileDirect("scripts/preupgrade", tpl.getTemplate("scripts/preupgrade"), 0755);
			eSpkTarGz.addFileDirect("scripts/postupgrade", tpl.getTemplate("scripts/postupgrade"), 0755);
			eSpkTarGz.addFileDirect("scripts/preuninst", tpl.getTemplate("scripts/preuninst"), 0755);
			eSpkTarGz.addFileDirect("scripts/postuninst", tpl.getTemplate("scripts/postuninst"), 0755);
			eSpkTarGz.addTextFile("scripts/installer", generateInstaller(), 0755);
			eSpkTarGz.addTextFile("scripts/start-stop-status", generateStartStopStatus(), 0755);
			getLog().info(String.format("Successfully packed %s", spkFile));

		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage());
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new MojoExecutionException(e.getMessage());
		} finally {
			if (spkTarGz != null) {
				spkTarGz.close();
			}
		}
		
	}

}
