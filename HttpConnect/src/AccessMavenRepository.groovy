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
