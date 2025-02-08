package xyz.mon0mon.selfrestdocsswagger

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.resourceDetails
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MessageControllerTest @Autowired constructor(
    private val objectMapper: ObjectMapper
): RestDocsTestSupport() {

    @Test
    fun `POST 메시지 API 테스트 및 문서 생성`() {
        val sampleRequest = SampleRequest(message = "안녕하세요!")
        val requestJson = objectMapper.writeValueAsString(sampleRequest)

        mockMvc.perform(
            post("/api/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    identifier = "message-post",
                    resourceDetails = resourceDetails()
                        .tag("Message API")
                        .summary("메시지 생성 API")
                        .description("POST /api/message 호출 시 요청 데이터를 받아 생성된 응답 반환"),
                    snippets = arrayOf(
                        requestFields(
                            fieldWithPath("message").description("전송할 메시지")
                        ),
                        responseFields(
                            fieldWithPath("id").description("생성된 메시지 ID"),
                            fieldWithPath("message").description("응답 메시지")
                        ),
                    )
                )
            )
    }

    @Test
    fun `GET 메시지 API 테스트 및 문서 생성`() {
        mockMvc.perform(
            get("/api/message")
                .param("id", "1")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().string(containsString("Hello, World!"))
            )
            .andDo(
                document(
                    identifier = "message-get",
                    resourceDetails = resourceDetails()
                        .tag("Message API")
                        .summary("메시지 조회 API")
                        .description("GET /api/message 호출 시 샘플 응답 반환"),
                    snippets = arrayOf(
                        queryParameters(
                            parameterWithName("id").description("메시지 ID")
                        ),
                        responseFields(
                            fieldWithPath("id").description("메시지 ID"),
                            fieldWithPath("message").description("응답 메시지")
                        )
                    )
                )
            )
    }
}
