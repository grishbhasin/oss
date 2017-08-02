## Inserting inclusion patterns

Use this API to insert patterns in the inclusion list.

**Requirement:** OpenSearchServer v1.5

### Call parameters

**URL:** ```/services/rest/index/{index_name}/crawler/web/patterns/inclusion```

**Method:** ```PUT```

**HTTP Header**:
- _**Content-Type**_ (required): ```application/json```
- _**Accept**_ (optional returned type): ```application/json``` or ```application/xml```

**URL parameters:**
- _**index_name**_ (required): The name of the index.
- _**replace**_ (optional): Set it to _true_ to replace all the patterns already present.
- _**inject_urls**_ (optional)=Set it to _true_ to inject the derived URL in the URL database.

**Raw data (PUT):**
An array of patterns.

    [
      "http://www.open-search-server.com/*",
      "https://github.com/jaeksoft/opensearchserver/*",
      "https://sourceforge.net/projects/opensearchserve/*"
    ]
    

### Success response
The patterns has been inserted.

**HTTP code:**
200

**Content (application/json):**

    {
        "successful": true,
        "info": "3 patterns injected"
    }
    

### Error response

The insertion failed. The reason is provided in the content.

**HTTP code:**
500

### Sample call

**Using CURL:**

    curl -XPUT -H "Content-Type: application/json" \
        -d '["http://www.open-search-server.com/*"]' \
        http://localhost:8080/services/rest/index/my_index/crawler/web/patterns/inclusion
    