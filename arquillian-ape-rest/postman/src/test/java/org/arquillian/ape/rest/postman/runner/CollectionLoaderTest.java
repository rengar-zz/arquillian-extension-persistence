package org.arquillian.ape.rest.postman.runner;


import org.arquillian.ape.rest.postman.runner.model.Body;
import org.arquillian.ape.rest.postman.runner.model.Collection;
import org.arquillian.ape.rest.postman.runner.model.Folder;
import org.arquillian.ape.rest.postman.runner.model.FormParameter;
import org.arquillian.ape.rest.postman.runner.model.Information;
import org.arquillian.ape.rest.postman.runner.model.ItemItem;
import org.arquillian.ape.rest.postman.runner.model.Method;
import org.arquillian.ape.rest.postman.runner.model.Mode;
import org.arquillian.ape.rest.postman.runner.model.Request;
import org.arquillian.ape.rest.postman.runner.model.Url;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CollectionLoaderTest {

    @Test
    public void should_read_info_field() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/SimpleRequest.json", new HashMap<>());

        // then
        Information expectedInformation = new Information("book", new URL("https://schema.getpostman.com/json/collection/v2.0.0/collection.json"), null);
        assertThat(collection.getInfo())
                .isNotNull()
                .isEqualToIgnoringNullFields(expectedInformation);
    }

    @Test
    public void should_read_simple_requests() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/SimpleRequest.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).extracting("name", "request")
                .containsExactly(
                        tuple("This is one request", new URL("http://localhost:8080"))
                );
    }

    @Test
    public void should_read_complex_get_request() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexGetRequest.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);

        Request expectedRequest = new Request();
        expectedRequest.setUrl(new Url(new URL("http://192.168.99.100:8080/?a=a")));
        expectedRequest.setMethod(Method.GET);
        expectedRequest.setBody(new Body());

        ItemItem expectedItemItem = new ItemItem();
        expectedItemItem.setName("This is one request");
        expectedItemItem.setRequestObject(expectedRequest);

        assertThat(itemItem).isEqualToComparingFieldByFieldRecursively(expectedItemItem);

    }

    @Test
    public void should_read_complex_get_request_with_headers() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexGetRequestWithHeaders.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);

        Request expectedRequest = new Request();
        expectedRequest.setUrl(new Url(new URL("http://192.168.99.100:8080/?a=a")));
        expectedRequest.setMethod(Method.GET);
        expectedRequest.setBody(new Body());
        Map<String, String> expexctedHeaders = new HashMap<>();
        expexctedHeaders.put("Content-Type", "application/json");
        expexctedHeaders.put("Authorization", "Basic YWE6YmI=");
        expectedRequest.setHeaders(expexctedHeaders);

        ItemItem expectedItemItem = new ItemItem();
        expectedItemItem.setName("This is one request");
        expectedItemItem.setRequestObject(expectedRequest);

        assertThat(itemItem).isEqualToComparingFieldByFieldRecursively(expectedItemItem);
    }

    @Test
    public void should_read_complex_post_with_body() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexPostRequestWithBody.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);

        assertThat(itemItem.getRequestObject().getBody().getMode()).isEqualTo(Mode.raw);
        assertThat(itemItem.getRequestObject().getBody().getRaw()).isEqualTo("{    \"name\": \"test\"  }");
    }

    @Test
    public void should_read_complex_post_with_body_form_data() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexPostRequestWithBodyFormData.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);

        assertThat(itemItem.getRequestObject().getBody().getMode()).isEqualTo(Mode.formdata);
        assertThat(itemItem.getRequestObject().getBody().getFormdata())
                .extracting(FormParameter::getKey, FormParameter::getValue)
                .contains(tuple("username", "aaa"));
    }

    @Test
    public void should_read_complex_url() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexUrlGetRequest.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);
        assertThat(itemItem.getRequestObject().getUrl().asNativeUrl())
                .hasProtocol("http")
                .hasHost("192.168.99.100")
                .hasPath("/path/path2")
                .hasQuery("a=b");
    }

    @Test
    public void should_replace_variables() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/ComplexGetRequestVariables.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        final ItemItem itemItem = (ItemItem) collection.getItem().get(0);
        assertThat(itemItem.getRequestObject().getUrl().asNativeUrl())
                .hasHost("localhost");

        final Map<String, String> headers = itemItem.getRequestObject().getHeaders();
        assertThat(headers).hasSize(1);

        assertThat(headers)
                .containsValue("Basic XXX");
    }

    @Test
    public void should_load_folders() throws IOException {
        // given
        CollectionLoader collectionLoader = new CollectionLoader();

        // when
        final Collection collection = collectionLoader.load("/FolderSimpleRequest.json", new HashMap<>());

        // then
        assertThat(collection.getItem()).hasSize(1);
        Folder folder = ((Folder) collection.getItem().get(0));

        assertThat(folder.getName()).isEqualTo("Folder");
        assertThat(folder.getItem()).hasSize(1);
    }

}
