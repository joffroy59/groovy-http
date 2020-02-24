import javax.net.ssl.HttpsURLConnection

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
	
	private getUrl(String val) {
		return val.replaceAll(~/\./, "/")
	}
	
	private String getMavenFilename(artefactId, version,type) {
		return "${artefactId}-${version}.${type}"
	}

	private String getUrl(groupId, artefactId, version, filename) {
		return "${repository}/${getUrl(groupId)}/${artefactId}/${version}/${filename}"
	}

	public void downloadMavenartefact(groupId, artefactId, version, type, outputFolder) {
		//TODO use a classifier
		def filename = getMavenFilename(artefactId, version,type)
		//nekohtml-1.9.22.jar
		def url = getUrl(groupId, artefactId, version,filename)
		download(url,outputFolder)
	}
	
	private void download(def address, def outputFolder) {
		new File("${outputFolder}").mkdirs()
		new File("${outputFolder}/${address.tokenize('/')[-1]}").withOutputStream { out ->
			out << new URL(address).openStream()
		}
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
downloadFolder="./download"
println "Download a Maven artefact : ${groupId}:${artefactId}:${version} to :${downloadFolder}"
myConnect.downloadMavenartefact(groupId, artefactId, version, type, downloadFolder)
println "Downloaded"
