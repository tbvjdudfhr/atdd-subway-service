package study.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.application.StationService;

@DisplayName("단위 테스트 - mockito를 활용한 가짜 협력 객체 사용")
public class MockitoTest {
	@Test
	void findAllLines() {
		// given
		LineRepository lineRepository = mock(LineRepository.class);
		StationService stationService = mock(StationService.class);

		Line line = Mockito.mock(Line.class);

		when(lineRepository.findAll()).thenReturn(Lists.newArrayList(line));
		LineService lineService = new LineService(lineRepository, stationService);

		// when
		List<LineResponse> responses = lineService.findLines();

		// then
		assertThat(responses).hasSize(1);
	}
}
