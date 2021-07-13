package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.domain.StationsResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static nextstep.subway.line.acceptance.LineAcceptanceTest.지하철_노선_등록되어_있음;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.지하철_노선에_지하철역_등록되어_있음;
import static nextstep.subway.station.StationAcceptanceTest.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |            (10)          |
     * *3호선*(3)                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = 지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = 지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10, 0);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10, 800);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 12, 900);

        지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);
    }

    @DisplayName("최단 경로를 조회한다.")
    @Test
    void findShortestPath() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청();

        // then
        PathResponse shortestPath = response.body().as(PathResponse.class);

        최단경로_조회_성공(response);
        최단경로에_포함됨(shortestPath, 남부터미널역, 교대역, 강남역);
        최단경로_길이가_구해짐(shortestPath);
        운임이_계산됨(shortestPath, 2250);
    }

    private void 운임이_계산됨(PathResponse shortestPath, int expectedFare) {
        assertThat(shortestPath.getFare()).isEqualTo(expectedFare);
    }

    private ExtractableResponse<Response> 최단경로_조회_요청() {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("sourceId", 남부터미널역.getId())
                .param("targetId", 강남역.getId())
                .when().get("/paths")
                .then().log().all().extract();
    }

    private void 최단경로_조회_성공(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 최단경로에_포함됨(PathResponse shortestPath, StationResponse...expectedStations) {
        StationsResponse stations = shortestPath.getStations();

        assertThat(stations.getStations()).containsExactly(
                expectedStations
        );
    }

    private void 최단경로_길이가_구해짐(PathResponse shortestPath) {
        assertThat(shortestPath.getDistance()).isEqualTo(13);
    }
}
