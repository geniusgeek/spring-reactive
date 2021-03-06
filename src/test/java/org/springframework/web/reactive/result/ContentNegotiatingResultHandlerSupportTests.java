/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.reactive.result;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.MockServerHttpRequest;
import org.springframework.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.reactive.accept.FixedContentTypeResolver;
import org.springframework.web.reactive.accept.HeaderContentTypeResolver;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.web.server.session.WebSessionManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.IMAGE_GIF;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;

/**
 * Unit tests for {@link ContentNegotiatingResultHandlerSupport}.
 * @author Rossen Stoyanchev
 */
public class ContentNegotiatingResultHandlerSupportTests {

	private TestHandlerSupport handlerSupport;

	private MockServerHttpRequest request;

	private ServerWebExchange exchange;


	@Before
	public void setUp() throws Exception {
		this.handlerSupport = new TestHandlerSupport();
		this.request = new MockServerHttpRequest(HttpMethod.GET, new URI("/path"));
		this.exchange = new DefaultServerWebExchange(
				this.request, new MockServerHttpResponse(), mock(WebSessionManager.class));
	}


	@Test
	public void usesContentTypeResolver() throws Exception {
		RequestedContentTypeResolver resolver = new FixedContentTypeResolver(IMAGE_GIF);
		TestHandlerSupport handlerSupport = new TestHandlerSupport(resolver);

		List<MediaType> mediaTypes = Arrays.asList(IMAGE_JPEG, IMAGE_GIF, IMAGE_PNG);
		MediaType actual = handlerSupport.selectMediaType(this.exchange, mediaTypes);

		assertEquals(IMAGE_GIF, actual);
	}

	@Test
	public void producibleMediaTypesRequestAttribute() throws Exception {
		Set<MediaType> producible = Collections.singleton(IMAGE_GIF);
		this.exchange.getAttributes().put(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, producible);

		List<MediaType> mediaTypes = Arrays.asList(IMAGE_JPEG, IMAGE_GIF, IMAGE_PNG);
		MediaType actual = handlerSupport.selectMediaType(this.exchange, mediaTypes);

		assertEquals(IMAGE_GIF, actual);
	}

	@Test  // SPR-9160
	public void sortsByQuality() throws Exception {
		this.request.getHeaders().add("Accept", "text/plain; q=0.5, application/json");

		List<MediaType> mediaTypes = Arrays.asList(TEXT_PLAIN, APPLICATION_JSON_UTF8);
		MediaType actual = this.handlerSupport.selectMediaType(this.exchange, mediaTypes);

		assertEquals(APPLICATION_JSON_UTF8, actual);
	}

	@Test
	public void charsetFromAcceptHeader() throws Exception {
		MediaType text8859 = MediaType.parseMediaType("text/plain;charset=ISO-8859-1");
		MediaType textUtf8 = MediaType.parseMediaType("text/plain;charset=UTF-8");

		this.request.getHeaders().setAccept(Collections.singletonList(text8859));
		MediaType actual = this.handlerSupport.selectMediaType(this.exchange, Collections.singletonList(textUtf8));

		assertEquals(text8859, actual);
	}

	@Test // SPR-12894
	public void noConcreteMediaType() throws Exception {
		List<MediaType> producible = Collections.singletonList(ALL);
		MediaType actual = this.handlerSupport.selectMediaType(this.exchange, producible);

		assertEquals(APPLICATION_OCTET_STREAM, actual);
	}




	private static class TestHandlerSupport extends ContentNegotiatingResultHandlerSupport {

		protected TestHandlerSupport() {
			this(new HeaderContentTypeResolver());
		}

		public TestHandlerSupport(RequestedContentTypeResolver contentTypeResolver) {
			super(new GenericConversionService(), contentTypeResolver);
		}
	}

}
