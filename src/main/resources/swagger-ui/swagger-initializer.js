// swagger-ui WebJar의 기본 초기화 스크립트를 대체한다.
// REST Docs 문서 테스트로 생성한 명세를 현재 origin 기준 상대 경로로 읽는다.
window.onload = function () {
    window.ui = SwaggerUIBundle({
        url: "./openapi3.json",
        dom_id: "#swagger-ui",
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
    });
};
