#!/usr/bin/env groovy
package io.vandorp
import groovy.json.*
import java.nio.file.Files

/*
* This file exist to help with debuging and overall structure.
* It seems to be the case that the code in `vars/*.groovy`
* cannot make calls to 'echo'. See the following comment.
* https://issues.jenkins-ci.org/browse/JENKINS-41953?focusedCommentId=307292&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-307292
*/

/**
* GitHubRelease
*/
class GitHubRelease {

    String githubToken
    String owner
    String repo

    /**
    * https://developer.github.com/v3/repos/releases/#create-a-release
    * @param tagName - The name of the tag
    * @param targetCommitish - The name of the branch or commit-hash from which we want to build the release
    * @param releaseDescription - The description of the release
    * @param isDraft - A boolean which marks this release as a draft
    * @param isPrerelease - A boolean which marks this release as a Prerelease
    *
    * @return String releaseID
    */
    String createRelease(String tagName, String targetCommitish, String releaseDescription, boolean isDraft, boolean isPrerelease) {
        echo "echo createRelease"
        def apiUrl = new URL("https://api.github.com/repos/${owner}/${repo}/releases")
        def HttpURLConnection connection = apiUrl.openConnection()
        connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.connect()

        def postBody = [:]
        postBody.put('tag_name', tagName)
        postBody.put('target_commitish', targetCommitish)
        postBody.put('name', tagName)
        postBody.put('body', releaseDescription)
        postBody.put('draft', isDraft)
        postBody.put('prerelease', isPrerelease)

        def json = JsonOutput.toJson(postBody)
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())
        writer.write(json)
        writer.flush()

        InputStream response = connection.getInputStream()
        Scanner scanner = new Scanner(response)
        String responseBody = scanner.useDelimiter("\\A").next()
        connection.disconnect()
        def jsonSlurper = new JsonSlurper()
        def jsonResponse = jsonSlurper.parseText(responseBody)
        String releaseId = jsonResponse['id']
        echo "echo createRelease ${releaseId}"
        return releaseId
    }

    /**
    * https://developer.github.com/v3/repos/releases/#get-a-single-release
    * @param releaseId - The releaseId which is returned when creating a new release.
    *
    * @return String uploadUrl
    */
    String getUploadUrl(String releaseId) {
        def apiUrl = new URL("https://api.github.com/repos/${owner}/${repo}/releases/${releaseId}")
        def HttpURLConnection connection = apiUrl.openConnection()
        connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
        InputStream response = connection.getInputStream()
        Scanner scanner = new Scanner(response)
        String responseBody = scanner.useDelimiter("\\A").next()
        connection.disconnect()
        def jsonSlurper = new JsonSlurper()
        def jsonResponse = jsonSlurper.parseText(responseBody)
        String uploadUrl = jsonResponse['upload_url']
        return uploadUrl
    }

    /**
    * https://developer.github.com/v3/repos/releases/#upload-a-release-asset
    * @param uploadUrl
    * @param fileName
    */
    def uploadArtifact(String uploadUrl, String fileName) {
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
        String uploadUrlPrefix = uploadUrl.split('\\{\\?')[0]
        def apiUrl = new URL("${uploadUrlPrefix}?name=${fileName}")

        def HttpURLConnection connection = apiUrl.openConnection()
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Cache-Control", "no-cache")
        connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
        connection.setRequestProperty('Content-Type', 'application/zip')
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.connect()

        File binaryFile = new File(fileName)

        OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output), true);

        // Send binary file.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(binaryFile.toPath(), output);
        output.flush(); // Important before continuing with writer!
        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
        // End of multipart/form-data.
        writer.append("--" + boundary + "--").append(CRLF).flush();

        InputStream response = connection.getInputStream()
        Scanner scanner = new Scanner(response)
        String responseBody = scanner.useDelimiter("\\A").next()
        connection.disconnect()
        return responseBody
    }
}
