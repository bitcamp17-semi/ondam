package data.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import data.repository.AlarmEmitterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmService {
	private final AlarmEmitterRepository emitterRepository;

    public void sendScheduleNotification(Long userId, String scheduleTitle) {
        SseEmitter emitter = emitterRepository.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("schedule")
                        .data("새 일정 등록: " + scheduleTitle));
            } catch (IOException e) {
                emitterRepository.remove(userId);
            }
        }
    }

}
