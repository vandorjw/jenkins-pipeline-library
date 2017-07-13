#!/usr/bin/groovy

/**
 * Adds a comment on the PR in github
 * @param comment - The comment to leave in the comment section. Should not be empty.
 * @param pr - The pull-request id, should be int.
 * @param project - The github project , should be string.
 */
def call(String filename, String pr, String project) {
    withCredentials([[$class: 'StringBinding', credentialsId: 'github_oath_token', variable: 'GITHUB_ACCESS_TOKEN']]) {
        def githubToken = "${GITHUB_ACCESS_TOKEN}"
        def apiUrl = new URL("https://api.github.com/repos/${project}/issues/${pr}/comments")
        echo "adding ${comment} to ${apiUrl}"
        try {
            def HttpURLConnection connection = apiUrl.openConnection()
            if (githubToken.length() > 0) {
                connection.setRequestProperty("Authorization", "Bearer ${githubToken}")
            }
            connection.setRequestMethod("POST")
            connection.setDoOutput(true)
            connection.connect()
            echo "Going to read file ${filename}"
            def fileContent = readFile "${filename}"
            echo "${fileContent}"
            // String encoded = fileContent.bytes.encodeBase64().toString()

            def body = "{\"body\":\"${fileContent}\"}"

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())
            writer.write(body)
            writer.flush()

            // execute the POST request
            new InputStreamReader(connection.getInputStream())

            connection.disconnect()
        } catch (err) {
            echo "ERROR  ${err}"
        }
    }
}
