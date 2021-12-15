package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.acceptance.LineSectionAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_되어_있음;
import static nextstep.subway.member.MemberAcceptanceTest.*;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class PathAcceptanceTest extends AcceptanceTest {
    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private LineResponse 일호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 서울역;
    private StationResponse 선바위역;
    private StationResponse 존재하지_않는_역;
    private StationResponse 소요산역;
    private StationResponse 신도림역;


    /**
     * 교대역    --- *2호선(10)* ---   강남역
     * |                           |
     * *3호선(3)*                   *신분당선(10)*
     * |                           |
     * 남부터미널역  --- *3호선(2)* --- 양재역
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = 지하철역_등록되어_있음("강남역");
        양재역 = 지하철역_등록되어_있음("양재역");
        교대역 = 지하철역_등록되어_있음("교대역");
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역");
        소요산역 = 지하철역_등록되어_있음("소요산역");
        신도림역 = 지하철역_등록되어_있음("신도림역");

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 강남역, 양재역, 10, 900);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-red-600", 교대역, 강남역, 10, 500);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-red-600", 교대역, 양재역, 5, 0);
        일호선 = 지하철_노선_등록되어_있음("일호선", "bg-red-600", 소요산역, 신도림역, 58, 0);

        지하철_노선에_지하철역_등록되어_있음(삼호선, 교대역, 남부터미널역, 3);
    }

    @DisplayName("두 역의 최단 거리 경로를 조회시 지하철 이용 요금도 함께 응답한다.")
    @Test
    void getPathsWithFare() {
        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(강남역, 남부터미널역);

        // then
        최단_경로_응답됨(조회_요청);
        최단_경로_조회됨(조회_요청, Arrays.asList(강남역, 양재역, 남부터미널역));
        최단_경로_계산됨(조회_요청, 12);
        최단_경로_요금_계산됨(조회_요청, 2250);
    }

    @DisplayName("기본 운임 요금을 응답한다.")
    @Test
    void getFare() {
        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(교대역, 남부터미널역);

        // then
        최단_경로_요금_계산됨(조회_요청, 1250);
    }

    @DisplayName("50km 초과 시 (8km 마다 100원) 요금을 응답한다.")
    @Test
    void getFare2() {
        // when
       ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(소요산역, 신도림역);

        // then
        최단_경로_요금_계산됨(조회_요청, 1450);
    }

    @DisplayName("청소년 요금을 응답한다.")
    @Test
    void getFare3() {
        //given
        회원_생성을_요청(EMAIL, PASSWORD, TEENAGER_AGE);
        String 토큰 = 로그인_되어_있음(EMAIL, PASSWORD);
        // when
        ExtractableResponse<Response> 조회_요청 = 내_최단_경로_조회_요청(교대역, 남부터미널역, 토큰);

        // then
        최단_경로_요금_계산됨(조회_요청, 720);
    }

    @DisplayName("어린이 요금을 응답한다.")
    @Test
    void getFare4() {
        //given
        회원_생성을_요청(EMAIL, PASSWORD, CHILD_AGE);
        String 토큰 = 로그인_되어_있음(EMAIL, PASSWORD);
        // when
        ExtractableResponse<Response> 조회_요청 = 내_최단_경로_조회_요청(교대역, 남부터미널역, 토큰);

        // then
        최단_경로_요금_계산됨(조회_요청, 450);
    }

    @DisplayName("역과 역 사이의 최단 경로를 조회한다.")
    @Test
    void getPaths() {
        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(강남역, 남부터미널역);

        // then
        최단_경로_응답됨(조회_요청);
        최단_경로_조회됨(조회_요청, Arrays.asList(강남역, 양재역, 남부터미널역));
        최단_경로_계산됨(조회_요청, 12);
    }

    @DisplayName("출발역과 도착역이 같은 경우 최단 경로를 조회한다.")
    @Test
    void getPaths2() {
        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(강남역, 강남역);

        // then
        최단_경로_응답_실패됨(조회_요청);
    }

    @DisplayName("출발역과 도착역이 연결이 되어 있지 않은 경우 최단 경로를 조회한다.")
    @Test
    void getPaths3() {
        //given
        서울역 = 지하철역_등록되어_있음("서울역");
        선바위역 = 지하철역_등록되어_있음("선바위역");
        지하철_노선_등록되어_있음("사호선", "bg-blue-600", 서울역, 선바위역, 10, 500);

        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(강남역, 선바위역);

        // then
        최단_경로_응답_실패됨(조회_요청);
    }

    @DisplayName("존재하지 않은 출발역이나 도착역 경우 최단 경로를 조회한다.")
    @Test
    void getPaths4() {
        //given
        존재하지_않는_역 = 존재하지_않는_역_생성();

        // when
        ExtractableResponse<Response> 조회_요청 = 최단_경로_조회_요청(강남역, 존재하지_않는_역);

        // then
        최단_경로_응답_실패됨(조회_요청);
    }

    private void 최단_경로_요금_계산됨(ExtractableResponse<Response> response, int expectedFare) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse).extracting("fare").isEqualTo(expectedFare);
    }

    private StationResponse 존재하지_않는_역_생성() {
        return StationResponse.of(new Station("존재하지 않는 역"));
    }

    private void 최단_경로_응답_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 최단_경로_계산됨(ExtractableResponse<Response> response, int expectedDistance) {
        PathResponse pathResponse = response.as(PathResponse.class);
        assertThat(pathResponse).extracting("distance").isEqualTo(expectedDistance);
    }

    private void 최단_경로_조회됨(ExtractableResponse<Response> response, List<StationResponse> expectedStations) {
        PathResponse pathResponse = response.as(PathResponse.class);
        List<Long> stationIds = pathResponse.getStations().stream()
                .map(station -> station.getId())
                .collect(Collectors.toList());
        List<Long> expectedStationIds = expectedStations.stream()
                .map(station -> station.getId())
                .collect(Collectors.toList());
        assertThat(stationIds).isEqualTo(expectedStationIds);
    }

    private void 최단_경로_응답됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private ExtractableResponse<Response> 최단_경로_조회_요청(StationResponse upStation, StationResponse downStation) {
        return RestAssured
                .given().log().all()
                .param("source", upStation.getId())
                .param("target", downStation.getId())
                .when().get("/paths")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 내_최단_경로_조회_요청(StationResponse upStation, StationResponse downStation, String token) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(token)
                .param("source", upStation.getId())
                .param("target", downStation.getId())
                .when().get("/paths")
                .then().log().all()
                .extract();
    }

    public static void 지하철_노선에_지하철역_등록되어_있음(LineResponse line, StationResponse upStation, StationResponse downStation, int distance) {
        LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청(line, upStation, downStation, distance);
    }

    public static LineResponse 지하철_노선_등록되어_있음(String lineName, String color, StationResponse upStation, StationResponse downStation, int distance, int extraFare) {
        return LineAcceptanceTest.지하철_노선_등록되어_있음(new LineRequest(lineName, color, upStation.getId(), downStation.getId(), distance, extraFare)).as(LineResponse.class);
    }

    public static StationResponse 지하철역_등록되어_있음(String name) {
        return StationAcceptanceTest.지하철역_등록되어_있음(name).as(StationResponse.class);
    }
}
