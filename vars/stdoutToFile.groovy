#!/usr/bin/groovy

/**
 * Adds a comment on the PR in github
 * @param command - The shell command of which we want to capture output
 * @param flepath - The file to which we want to save the output
 */
def call(String command, String filepath) {
        def stdout = sh(script: command, returnStdout: true)
        writeFile file: filepath, text: stdout
        } catch (err) {
            echo "ERROR  ${err}"
        }
    }
}
