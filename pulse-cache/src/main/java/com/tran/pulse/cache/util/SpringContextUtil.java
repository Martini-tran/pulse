package com.tran.pulse.cache.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 *
 * @author tran
 * @version 1.0.0.0
 * @date 2025/7/1 13:45
 * @since 1.0
 */
public class SpringContextUtil implements ApplicationContextAware {

    /**
     * Spring应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 设置Spring应用上下文
     * Spring容器会在启动时自动调用此方法，注入ApplicationContext实例
     * 
     *
     * @param applicationContext Spring应用上下文
     * @throws BeansException 如果上下文设置失败
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取Spring应用上下文
     *
     * @return ApplicationContext Spring应用上下文实例
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext未初始化，请确保SpringContextUtil已被Spring容器加载");
        }
        return applicationContext;
    }

    /**
     * 通过Bean名称获取Bean实例
     * 
     * 根据Bean的名称从Spring容器中获取对应的Bean实例。
     * 如果存在多个相同类型的Bean，可以通过名称来区分获取特定的Bean。
     * 
     *
     * @param name Bean的名称
     * @return Object Bean实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过Class类型获取Bean实例
     * 根据Bean的类型从Spring容器中获取对应的Bean实例。
     * 如果容器中存在多个相同类型的Bean，将抛出异常。
     * 
     *
     * @param clazz Bean的Class类型
     * @param <T>   Bean的类型
     * @return T Bean实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定类型的Bean
     * @throws org.springframework.beans.factory.NoUniqueBeanDefinitionException 如果找到多个相同类型的Bean
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过Bean名称和Class类型获取Bean实例
     * 根据Bean的名称和类型从Spring容器中获取对应的Bean实例。
     * 这种方式最为精确，既指定了Bean的名称又指定了类型，避免了歧义。
     * 
     *
     * @param name  Bean的名称
     * @param clazz Bean的Class类型
     * @param <T>   Bean的类型
     * @return T Bean实例
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     * @throws org.springframework.beans.BeanNotOfRequiredTypeException 如果Bean不是指定的类型
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 判断Spring容器中是否包含指定名称的Bean
     * 
     * 检查Spring容器中是否存在指定名称的Bean定义。
     * 注意：即使返回true，也不保证getBean()一定能成功，因为Bean可能在创建时失败。
     * 
     *
     * @param name Bean的名称
     * @return boolean 如果容器中包含指定名称的Bean返回true，否则返回false
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例模式
     * 
     * 检查指定名称的Bean是否被配置为单例模式（singleton）。
     * 在Spring中，默认的Bean作用域是单例模式。
     * 
     *
     * @param name Bean的名称
     * @return boolean 如果是单例模式返回true，否则返回false
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static boolean isSingleton(String name) {
        return getApplicationContext().isSingleton(name);
    }

    /**
     * 获取指定名称的Bean的Class类型
     * 
     * 获取Spring容器中指定名称的Bean的实际类型。
     * 这在运行时动态确定Bean类型时非常有用。
     * 
     *
     * @param name Bean的名称
     * @return Class<?> Bean的Class类型，如果无法确定类型则返回null
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static Class<?> getType(String name) {
        return getApplicationContext().getType(name);
    }

    /**
     * 判断是否为原型模式的Bean
     * 
     * 检查指定名称的Bean是否被配置为原型模式（prototype）。
     * 原型模式的Bean每次获取都会创建新的实例。
     * 
     *
     * @param name Bean的名称
     * @return boolean 如果是原型模式返回true，否则返回false
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有找到指定名称的Bean
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static boolean isPrototype(String name) {
        return getApplicationContext().isPrototype(name);
    }

    /**
     * 获取指定类型的所有Bean名称
     * 
     * 获取Spring容器中所有指定类型（包括子类型）的Bean名称数组。
     * 
     *
     * @param type Bean的类型
     * @return String[] Bean名称数组，如果没有找到则返回空数组
     * @throws IllegalStateException 如果ApplicationContext未初始化
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getApplicationContext().getBeanNamesForType(type);
    }

    /**
     * 判断ApplicationContext是否已经初始化
     *
     * @return boolean 如果已初始化返回true，否则返回false
     */
    public static boolean isInitialized() {
        return applicationContext != null;
    }
}