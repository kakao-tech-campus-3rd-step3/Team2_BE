package kr.it.pullit.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String EXCHANGE_NAME = "question.exchange";
  public static final String QUEUE_NAME = "question.generation.queue";
  public static final String ROUTING_KEY = "question.generation.request";

  public static final String COMPLETION_EXCHANGE_NAME = "question.completion.exchange";
  public static final String COMPLETION_QUEUE_NAME = "question.completion.queue";
  public static final String COMPLETION_ROUTING_KEY = "question.completion.success";

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  // org.springframework.amqp.core.Queue 사용!
  @Bean
  public Queue queue() {
    // durable=true 로 생성 (필요에 맞게 exclusive, autoDelete 조정 가능)
    return new Queue(QUEUE_NAME, true);
  }

  @Bean
  public Binding binding(
      @Qualifier("queue") Queue queue, @Qualifier("exchange") TopicExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
  }

  @Bean
  public TopicExchange completionExchange() {
    return new TopicExchange(COMPLETION_EXCHANGE_NAME);
  }

  @Bean
  public Queue completionQueue() {
    return new Queue(COMPLETION_QUEUE_NAME, true);
  }

  @Bean
  public Binding completionBinding(
      @Qualifier("completionQueue") Queue completionQueue,
      @Qualifier("completionExchange") TopicExchange completionExchange) {
    return BindingBuilder.bind(completionQueue).to(completionExchange).with(COMPLETION_ROUTING_KEY);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);
    return rabbitTemplate;
  }

  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
