package com.example.dell.wi_fi_direct_based_videostream_ltf.Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * JAVA对象&Byte数组之间转化
 *
 * @author liqqc
 */
public class Object2Array {
//    private static final Logger LOGGER = LoggerFactory.getLogger(Object2Array.class);

//    public static void main(String[] args) {
//        Person person = new Person("Jone", 25, "good person");
//        byte[] objectToByteArray = objectToByteArray(person);
//        System.err.println("objectToByteArray: " + objectToByteArray);
//        Object byteArrayToObject = byteArrayToObject(objectToByteArray);
//        System.err.println("byteArrayToObject: " + byteArrayToObject.toString());
//    }

    /**
     * 对象转Byte数组
     *
     * @param obj
     * @return
     */
    public static byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
//            LOGGER.error("objectToByteArray failed, " + e);
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
//                    LOGGER.error("close objectOutputStream failed, " + e);
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
//                    LOGGER.error("close byteArrayOutputStream failed, " + e);
                    e.printStackTrace();
                }
            }

        }
        return bytes;
    }

    /**
     * Byte数组转对象
     *
     * @param bytes
     * @return
     */
    public static Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
//            LOGGER.error("byteArrayToObject failed, " + e);
            e.printStackTrace();
        } finally {

            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
//                    LOGGER.error("close byteArrayInputStream failed, " + e);
                    e.printStackTrace();
                }

            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
//                    LOGGER.error("close objectInputStream failed, " + e);
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    static class Person implements Serializable {
        private static final long serialVersionUID = 3559533002594201715L;
        private String name;
        private Integer age;
        private String desc;

        public Person(String name, int age, String desc) {
            this.name = name;
            this.age = age;
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "Person [name=" + name + ", age=" + age + ", desc=" + desc + "]";
        }

    }
}
