package com.rich.richsynapsehub.utils.ai.chatMeory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义对话持久化存储到文件，实现 ChatMemory 接口
 * 用法：ChatMemory https://docs.spring.io/spring-ai/reference/api/chat-memory.html#_quick_start
 * Kryo 序列化 https://juejin.cn/post/6993647089431347237
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/4
 **/
public class FileChatMemory implements ChatMemory {

    private final String BASE_DIR_URL;
    private static final Kryo kryo = new Kryo();

    /**
     * 初始化 Kryo 序列化器
     **/
    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 初始化持久化存储到的文件
     *
     * @param dir
     * @return
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    public FileChatMemory(String dir) {
        // 初始化路径
        BASE_DIR_URL = dir;
        // 初始化文件夹
        File baseDir = new File(BASE_DIR_URL);
        // 如果文件夹不存在，创建文件夹
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 实现接口方法，添加消息到指定的聊天会话中
     *
     * @param chatId
     * @param newMessages
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public void add(String chatId, List<Message> newMessages) {
        // 获取已有历史消息
        List<Message> chatMessages = getOrCreateChat(chatId);
        // 录入新消息
        chatMessages.addAll(newMessages);
        // 保存消息
        saveChat(chatId, chatMessages);
    }

    /**
     * 实现接口方法，获取指定聊天会话的最后 N 条消息
     *
     * @param chatId
     * @param lastN
     * @return java.util.List<org.springframework.ai.chat.messages.Message>
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public List<Message> get(String chatId, int lastN) {
        List<Message> messages = getOrCreateChat(chatId);
        return messages.stream()
                .skip(Math.max(0, messages.size() - lastN))
                .toList();
    }

    /**
     * 实现接口方法，清除指定聊天会话的所有消息
     *
     * @param chatId
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public void clear(String chatId) {
        File file = getChatFile(chatId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 序列化，保存指定聊天会话的所有消息
     *
     * @param chatId
     * @param messages
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    private void saveChat(String chatId, List<Message> messages) {
        File file = getChatFile(chatId);
        // 转为输出流后，序列化写入文件，并自动关闭流
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 反序列化，获取指定聊天会话的所有消息
     *
     * @param chatId
     * @return java.util.List<org.springframework.ai.chat.messages.Message>
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    private List<Message> getOrCreateChat(String chatId) {
        File file = getChatFile(chatId);
        List<Message> messages = new ArrayList<>();
        // 如果文件不存在，直接返回空列表
        if (file.exists()) {
            // 转为输入流后，反序列化读取文件，并自动关闭流
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 获取指定聊天会话的文件路径
     *
     * @param chatId
     * @return java.io.File
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    private File getChatFile(String chatId) {
        // 若果目录不存在，创建目录；否则，直接返回文件
        return new File(BASE_DIR_URL, chatId + ".kryo");
    }
}
