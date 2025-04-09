package data.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class AlarmEmitterRepository {
//SseEmitter를 저장하기위한 파일	
	
	private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

	//userId를 key로 해서 SseEmitter 저장
    public void save(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
    }
    
    //userId로 사용자의 SseEmitter를 가져오기
    public SseEmitter get(Long userId) {
        return emitters.get(userId);
    }
    
    //userId의 Sse 제거
    public void remove(Long userId) {
        emitters.remove(userId);
    }
}
