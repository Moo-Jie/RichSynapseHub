package com.rich.richsynapsehub.utils.ai.chatMeory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 自定义对话持久化存储到 Redis，实现 ChatMemory 接口
 * 用法：ChatMemory https://docs.spring.io/spring-ai/reference/api/chat-memory.html#_quick_start
 * Kryo 序列化 https://juejin.cn/post/6993647089431347237
 *
 * @author DuRuiChi
 * @create 2025/7/25
 **/
public class RedisChatMemory implements ChatMemory {

    private final RedisTemplate<String, byte[]> redisTemplate;

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });

    public RedisChatMemory(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void add(String conversationId, Message message) {
        add(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> newMessages) {
        List<Message> existingMessages = getOrCreateChat(conversationId);
        existingMessages.addAll(newMessages);
        saveChat(conversationId, existingMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> messages = getOrCreateChat(conversationId);
        return messages.stream().skip(Math.max(0, messages.size() - lastN)).toList();
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(buildKey(conversationId));
    }

    private String buildKey(String conversationId) {
        String KEY_PREFIX = "chat:memory:";
        return KEY_PREFIX + conversationId;
    }

    private void saveChat(String conversationId, List<Message> messages) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); Output output = new Output(bos)) {
            kryoThreadLocal.get().writeObject(output, messages);
            output.flush();
            byte[] serialized = bos.toByteArray();
            ValueOperations<String, byte[]> ops = redisTemplate.opsForValue();
            ops.set(buildKey(conversationId), serialized, 7, TimeUnit.DAYS); // 设置7天过期
        } catch (IOException e) {
            throw new RuntimeException("Redis序列化失败", e);
        }
    }

    private List<Message> getOrCreateChat(String conversationId) {
        ValueOperations<String, byte[]> ops = redisTemplate.opsForValue();
        byte[] serialized = ops.get(buildKey(conversationId));
        if (serialized == null) return new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized); Input input = new Input(bis)) {
            return kryoThreadLocal.get().readObject(input, ArrayList.class);
        } catch (IOException e) {
            throw new RuntimeException("Redis反序列化失败", e);
        }
    }
}