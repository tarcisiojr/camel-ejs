# camel-ejs
This component allows you to process a message using an [EJS](https://github.com/tj/ejs) template.

## URI format
ejs:templateName

Where **templateName** is the classpath-local URI of the template to invoke; or the complete URL of the remote template (eg: file://folder/myfile.ejs).

You can append query options to the URI in the following format, ?option=value&option=value&...

## Message Headers

```
| Header              | Description                          |
| ------------------- | ------------------------------------ |
| CamelEJSTemplate    | Template string form header.         |
| CamelEJSResourceUri | The templateName as a String object. |
```

## EJS Context

Camel will provide exange information in then EJS template script like a Javascript Object. The Exchange is transfered as:

* **exchange**: The Exchange itself.
* **headers**: The headers of the In message.
* **request**: The in message.
* **in**: The in message.
* **body**: The in message body.

## Examples

