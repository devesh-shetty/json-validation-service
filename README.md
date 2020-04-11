# JSON validation service
A REST-service for validating JSON documents against JSON Schemas.

## How to run the project?
- Clone the project.
- `cd json-validation-service`
- `sbt run`

You could use [curl](https://github.com/curl/curl) or any other tool to interact with the service.
You could upload the JSON schema using `curl http://localhost/schema/config-schema -X POST -d @config-schema.json`.
<details>
<summary>config-schema.json</summary>
<pre>
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "source": {
      "type": "string"
    },
    "destination": {
      "type": "string"
    },
    "timeout": {
      "type": "integer",
      "minimum": 0,
      "maximum": 32767
    },
    "chunks": {
      "type": "object",
      "properties": {
        "size": {
          "type": "integer"
        },
        "number": {
          "type": "integer"
        }
      },
      "required": ["size"]
    }
  },
  "required": ["source", "destination"]
}

</pre>

</details>

Download an existing schema by schemaId using `curl http://localhost/schema/config-schema`

You could validate a JSON document against an existing schema id using `curl http://localhost/validate/config-schema -X POST -d @config.json`

<details>
<summary>config.json</summary>
<pre>
{
  "source": "/home/alice/image.iso",
  "destination": "/mnt/storage",
  "timeout": null,
  "chunks": {
    "size": 1024,
    "number": null
  }
}

</pre>
</details>

The server cleans the uploaded JSON document to remove keys for which the value is null.
