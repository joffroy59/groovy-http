import java.util.jar.JarEntry
import java.util.jar.JarFile

import groovy.xml.XmlUtil

class MyConnect {

	String repository
	
	public MyConnect(String repository) {
		this.repository = repository
	}
	
	public accessMavenRepository(String groupId, String artefactId) {
		String artefactRepoUri = "${repository}/${getUrl(groupId)}/${getUrl(artefactId)}/maven-metadata.xml"
		return new XmlSlurper().parse(artefactRepoUri)
		
	}
	
	public prettyPrint(xml) {
		return XmlUtil.serialize(xml)
	}
	
	public String getMavenFilename(artefactId, version, type) {
		return "${artefactId}-${version}.${type}"
	}

	public void downloadMavenartefact(groupId, artefactId, version, outputFolder, filename) {
		//TODO use a classifier
		def url = getUrl(groupId, artefactId, version, filename)
		download(url,outputFolder)
	}
	
	public extractJar(downloadFolder, filename) {
		println "exrtact ${filename} to ${downloadFolder+File.separator}}"
		def jar = new JarFile(filename)
		def jarFile = new File(jar.name)
		def distDir = "${downloadFolder+File.separator}"
		def libName = jarFile.name.with {it.take(it.lastIndexOf('.'))}
		new File("${distDir+libName}").with {
			if (!it.exists()) it.mkdirs()
		}
		distDir += libName+File.separator
		for (JarEntry file in jar.entries()){
			def f = new File("${distDir+file.name}")
			if (file.isDirectory()){
				f.mkdir()
				continue
			}
			def is = jar.getInputStream(file)
			f.withOutputStream { def stream ->
				while (is.available()>0)
					stream.write(is.read())
			}
			is.close()
		}
		jar.close()
		println "exrtacted to: ${downloadFolder}"
	}
	
	private void download(def address, def outputFolder) {
		new File("${outputFolder}").mkdirs()
		new File("${outputFolder}/${address.tokenize('/')[-1]}").withOutputStream { out ->
			out << new URL(address).openStream()
		}
	}

	private getUrl(String val) {
		return val.replaceAll(~/\./, "/")
	}
	
	private String getUrl(groupId, artefactId, version, filename) {
		return "${repository}/${getUrl(groupId)}/${artefactId}/${version}/${filename}"
	}

}

def myConnect = new MyConnect("https://dl.bintray.com/kdabir/glide");

metadata = myConnect.accessMavenRepository("io.github.kdabir.glide", "glide-gradle-plugin");
println myConnect.prettyPrint(metadata)
println metadata.versioning.versions.version*.text()
println metadata.versioning.latest

myConnect = new MyConnect("https://repo1.maven.org/maven2");

metadata = myConnect.accessMavenRepository("ai.h2o", "h2o-genmodel");
println myConnect.prettyPrint(metadata)
println metadata.versioning.versions.version*.text()
println metadata.versioning.latest

groupId="net.sourceforge.nekohtml"
artefactId="nekohtml"
version="1.9.22"
type="jar"
downloadFolder="download"
filename=myConnect.getMavenFilename(artefactId, version, type)
println "Download a Maven artefact : ${groupId}:${artefactId}:${version} to :${downloadFolder}"
myConnect.downloadMavenartefact(groupId, artefactId, version, downloadFolder, filename)
println "Downloaded ${filename}"

myConnect.extractJar("${downloadFolder+File.separator}", "${downloadFolder+File.separator+filename}")
