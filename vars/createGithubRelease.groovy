#!/usr/bin/env groovy
import groovy.json.*;

/**
 * Creates a new release in github using the github api
 * see: https://developer.github.com/v3/repos/releases/#create-a-release
 * @param project - The github project , should be string: "owner/project"
 * @param tag_name - Required. The name of the tag.
 * @param target_commitish - Specifies the commitish value that determines
                             where the Git tag is created from. Can be any
                             branch or commit SHA. Unused if the Git tag already
                             exists.
  * @param name - The name of the release.
  * @param body - Text describing the contents of the tag.
  * @param draft - true to create a draft (unpublished) release,
                   false to create a published one.
  * @param prerelease - true to identify the release as a prerelease.
                        false to identify the release as a full release.
 */
def call(String project, String tag_name, String target_commitish, String name, String body, Boolean draft, Boolean prerelease) {
    withCredentials([[$class: 'StringBinding', credentialsId: 'github_oath_token', variable: 'GITHUB_ACCESS_TOKEN']]) {
        def githubToken = "${GITHUB_ACCESS_TOKEN}"
        def apiUrl = new URL("https://api.github.com/repos/${project}/releases")
        try {
            def HttpURLConnection connection = apiUrl.openConnection()
            if (githubToken.length() > 0) {
                connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
            }
            connection.setRequestMethod("POST")
            connection.setDoOutput(true)
            connection.connect()

            def postBody = [:]
            postBody.put('tag_name', tag_name)
            postBody.put('target_commitish', target_commitish)
            postBody.put('name', name)
            postBody.put('body', body)
            postBody.put('draft', draft)
            postBody.put('prerelease', prerelease)

            def json = JsonOutput.toJson(postBody)

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(json)
            writer.flush()

            // execute the POST request
            new InputStreamReader(connection.getInputStream())

            connection.disconnect()
        } catch (err) {
            echo "ERROR  ${err}"
        }
    }
}
