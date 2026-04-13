package com.storm.ai.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storm.context.UserContext;
import com.storm.entity.TokenPayload;
import com.storm.service.OrderService;
import com.storm.service.TicketService;
import com.storm.service.UserService;
import com.storm.tools.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@Tag(name = "对话模块",description = "ai对话相关接口")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@RestController
@Validated
public class ChatController {

    //默认情况下，Spring AI 会自动配置单个bean。 不过，你可能需要在申请中同时使用多个聊天模型。 以下是处理这种情况的方法：ChatClient.Builder
    //无论哪种情况，你都需要通过设置属性 来禁用自动配置。ChatClient.Builderspring.ai.chat.client.enabled=false
    //已配置好的对话客户端
    private final ChatClient  userChatClient;

    //注入工具类
    private final AdminAssistanceTools adminAssistanceTools;
    private final RagAssistanceTools ragAssistanceTools;
    private final ChatClient gameChatClient;
    private final ChatClient ragChatClient;
    private final ChatClient adminChatClient;
    private final ChatClient normalchatClient;

    private final UserService userService;
    private final ObjectMapper objectMapper; // 使用 Jackson
    private final OrderService orderService;
    private final TicketService ticketService;


    @Operation(summary = "普通非流式单次对话")
    @GetMapping("talk")
    public Flux<String> talk(@RequestParam(value = "prompt",defaultValue = "你好") String prompt,
                             @RequestParam String sessionId
    ){
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("用户的提问是:{},会话id是:{}",prompt,sessionId);
        return normalchatClient
                //开始构建提示词
                .prompt()
                //放入用户消息
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                .user(prompt)
                //发送请求并获取响应
                .stream()
                // 从响应中提取文本内容
                .content();
                //如果这里是.chatResponse();那么返回值就是,ChatResponse chatResponse
    }

    @Operation(summary = "哄哄模拟器对话")
    @GetMapping("game")
   public Flux<String> girlFriend(@RequestParam(value = "prompt",defaultValue = "你好") String prompt,
                       @NotBlank(message = "会话ID不能为空") @RequestParam String sessionId){

        // 获取当前登录用户ID
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("用户的提问是:{},会话id是:{}",prompt,sessionId);
        log.info("用户的提问是:{}",prompt);
        return gameChatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                .user(prompt)
                .stream()
                .content();
    }

    @Operation(summary = "管理端智能助手")
    @GetMapping(value = "adminchat",produces = "text/plain;charset=UTF-8")
    public Flux<String> adminChat(@RequestParam(value = "prompt",defaultValue = "你好") String prompt,
                             @NotBlank(message = "会话ID不能为空") @RequestParam String sessionId){

        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();

        log.info("用户的提问是:{},会话id是:{}",prompt,sessionId);
        //其实你可以这样理解,配置类的链式调用是在配初始化参数,而这里的就是在和ai对话了
        //所以配置列里的defaultAdvisors是全局的,只要你用了那个bean,就一直有那个配置
        return  adminChatClient.prompt()//1.开始构建提示词
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                .advisors(new SimpleLoggerAdvisor())//此响应方法的增强配置
                .user(prompt)//开始放入用户问题
                .tools(adminAssistanceTools)
                .stream()//发送请求并获取响应
                .content();// 从响应中提取文本内容
    }



    /**
     * 1. 获取 MCP 服务暴露的所有工具（Tools）
     * MCP 服务（mcp-server-chart）会自动声明它能做什么（比如生成折线图、柱状图）。
     */
    //produces = "text/plain;charset=UTF-8"可以防止前端乱码
        @Operation(summary = "客服对话(意图智能识别)")
        @GetMapping(value = "chat",produces = "text/plain;charset=UTF-8")
        public Flux<String> chat(@RequestParam(value = "prompt",defaultValue = "你好") String prompt,
                                 @NotBlank(message = "会话ID不能为空") @RequestParam String sessionId){

            TokenPayload tokenPayload = UserContext.getUser();
            String userId = tokenPayload.getUserId();

            UserAssistanceTools userAssistanceTools=new UserAssistanceTools(userService,objectMapper,orderService,ticketService,userId);
            log.info("用户的提问是:{},会话id是:{}",prompt,sessionId);
            //其实你可以这样理解,配置类的链式调用是在配初始化参数,而这里的就是在和ai对话了
            //所以配置列里的defaultAdvisors是全局的,只要你用了那个bean,就一直有那个配置
            return  userChatClient.prompt()//1.开始构建提示词
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                        .advisors(new SimpleLoggerAdvisor())//此响应方法的增强配置
                    .user(prompt)//开始放入用户问题
                    .tools(userAssistanceTools,ragAssistanceTools)
                    .stream()//发送请求并获取响应
                    .content();// 从响应中提取文本内容
        }
    /*
    TODO 后期可能会实现单窗口支持多文件问答功能,(@文件名+后端解析文件名并过滤参数)
     目前入库阶段:从普通tokens切分-->递归符号切分,
     出库检索功能:从普通QuestionAnswerAdvisor-->retrievalAugmentationAdvisor模块化优化检索流程
     上述改善已对rag检索回答功能已有很大改善
     进阶:1.可能语义分割会更好?
         2.实现普通对话和rag智能识别切换?
     */
    @Operation(summary = "单纯RAG对话")
    @GetMapping("ragchat")
    public Flux<String> ragChat(
            @RequestParam(value = "prompt", defaultValue = "你好") String prompt,
            @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId) {

        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();

        //String filterExpression = String.format("file_name == '%s' ", filename);

        //log.info("当前检索过滤器: {}", filterExpression);

        log.info("用户的提问是:{},会话id是:{}",prompt,sessionId);

        return ragChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                .advisors(new SimpleLoggerAdvisor()) // 可选：打印完整 prompt 到日志
                //.advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression))
                .stream()
                .content();
    }

}
