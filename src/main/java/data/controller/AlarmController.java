package data.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import data.repository.AlarmEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {

	private final AlarmEmitterRepository emitterRepository;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(name = "userId") Long userId) {
        SseEmitter emitter = new SseEmitter(60 * 10000L); // 10분
        emitterRepository.save(userId, emitter);

        // 연결 종료 시 정리
        emitter.onCompletion(() -> emitterRepository.remove(userId));
        emitter.onTimeout(() -> emitterRepository.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결됨"));
        } catch (IOException e) {
            log.error("SSE 연결 초기 전송 실패", e);
        }
        return emitter;
    }
}