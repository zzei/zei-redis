package com.zei.redis.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * redis 切面
 * </p>
 *
 * @author zei
 * @since 2020年03月12日16:57:44
 */
@Component
@Aspect
@Slf4j
public class RedisCacheAspect {

    private static final String CACHE_KEY = "CACHE:";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定义@RedisCache
     */
    @Pointcut("@annotation(RedisCache)")
    public void redisServicePoint() {
    }

    /**
     * 环绕，拦截方法
     * el写法 #{#param} 、#{#obj.param}
     *
     * @param point
     * @return
     */
    @Around("redisServicePoint()")
    public Object LoadDataFromRedis(ProceedingJoinPoint point) {
        log.info("=========执行缓存操作=========");
        RedisCache redisCache = ((MethodSignature) point.getSignature()).getMethod().getAnnotation(RedisCache.class);
        if (redisCache != null && redisCache.read()) {
            //执行读请求操作
            String key = executeTemplate(redisCache.key(), point);
            if (StringUtils.isEmpty(key)) {
                log.error("=========key不能为空=========");
                return null;
            }
            key = CACHE_KEY + key;
            try {
                //先从缓存中查询
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj == null) {
                    //若缓存中不存在，则放行去数据库查询
                    log.info("=========缓存key: {} 无记录，查询数据库=========", key);
                    obj = point.proceed();
                    //将查询出的结果放进缓存
                    if (obj != null) {
                        redisTemplate.opsForValue().set(key, obj, redisCache.expired(), TimeUnit.SECONDS);
                    }
                } else {
                    log.info("=========缓存命中=========");
                }
                return obj;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else if (redisCache != null && !redisCache.read()) {
            //执行update、delete方法 则删除缓存
            String key = executeTemplate(redisCache.key(), point);
            if (StringUtils.isEmpty(key)) {
                log.error("=========key不能为空=========");
                return null;
            }
            key = CACHE_KEY + key;
            try {
                //删除缓存
                log.info("=========删除缓存key: {} =========", key);
                redisTemplate.delete(key);
                return point.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 解析SPEL
     *
     * @param data
     * @param joinPoint
     * @return
     */
    private String executeTemplate(String data, ProceedingJoinPoint joinPoint){

        ExpressionParser parser = new SpelExpressionParser();

        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        String[] params = discoverer.getParameterNames(method);

        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        return parser.parseExpression(data, new TemplateParserContext()).getValue(context, String.class);
    }
}
