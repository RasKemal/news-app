package com.example.newsapp.data.remote

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Verifies Retrofit + Gson parsing against a real HTTP shape using [MockWebServer].
 * No Android runtime required (plain JVM unit test).
 */
class SpaceflightApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getArticles parses Spaceflight-style JSON into DTOs`() = runBlocking {
        val body = """
            {
              "count": 1,
              "next": "https://api.example/v4/articles/?limit=10&offset=10",
              "previous": null,
              "results": [
                {
                  "id": 42,
                  "title": "Launch success",
                  "summary": "Mission completed nominally.",
                  "url": "https://example.com/article/42",
                  "image_url": "https://cdn.example/img.jpg",
                  "news_site": "SpaceNews",
                  "published_at": "2026-03-19T12:00:00Z"
                }
              ]
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )

        val response = api.getArticles(limit = 10, offset = 0, search = null)

        assertEquals(1, response.results.size)
        val article = response.results.first()
        assertEquals(42L, article.id)
        assertEquals("Launch success", article.title)
        assertEquals("https://cdn.example/img.jpg", article.imageUrl)
        assertEquals("SpaceNews", article.newsSite)
    }

    @Test
    fun `nullable fields deserialize as null without crashing`() = runBlocking {
        val body = """
            {
              "count": 1,
              "next": null,
              "previous": null,
              "results": [
                {
                  "id": 7,
                  "title": "Minimal article",
                  "summary": "",
                  "url": "https://example.com/7",
                  "news_site": null,
                  "published_at": null
                }
              ]
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )

        val article = api.getArticles(limit = 10, offset = 0, search = null).results.first()

        assertEquals(7L, article.id)
        assertNull(article.imageUrl)
        assertNull(article.newsSite)
        assertNull(article.publishedAt)
    }
}
