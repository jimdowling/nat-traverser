/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.kompics;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component will auto-subscribe any handlers declared in a subclass
 * to any ports declared in the subclass, so long as that port defines the
 * handler's event.
 *
 * Note: if you have a SecurityManager installed, some of the reflection code
 * setting field accessibility to be true will fail unless you explicitly
 * specify permission for this class to set field accessibility.
 *
 * Note: Your port type definitions must extend PortType directly - multi-level
 * inheritence from PortType not supported.
 *
 * Your component must inherit directly from AutoSubscribeComponent, multi-level
 * inheritence not supported.
 *
 * @author jdowling
 */
public abstract class AutoSubscribeComponent extends ComponentDefinition {

    private static final String HAS_POSITIVE = "hasPositive";
    private static final String HAS_NEGATIVE = "hasNegative";

    protected class HandlerReflection {

        private final Class<? extends Event> eventType;
        private final Field handler;

        public HandlerReflection(Class<? extends Event> eventType, Field handler) {
            this.eventType = eventType;
            this.handler = handler;
        }

        public Class<? extends Event> getEventType() {
            return eventType;
        }

        public Field getHandler() {
            return handler;
        }
    }

    protected class PortReflection {

        private final Class<? extends PortType> portType;
        private final Field port;

        public PortReflection(Class<? extends PortType> portType, Field port) {
            this.portType = portType;
            this.port = port;
        }

        public Class<? extends PortType> getPortType() {
            return portType;
        }

        public Field getPortField() {
            return port;
        }
    }
    protected boolean autoSubscribed = false;

    public AutoSubscribeComponent() {
    }

    protected void autoSubscribe() {
        autoSubscribe(new HashMap<String, String>());
    }

    /**
     * Called in the constructor of a subclass of AutoSubscribeComponent.
     * Cannot be called from the constructor of AutoSubscribeComponent, as fields
     * for the subclass have not been initialized when it is called.
     *
     * @param portHandler map of <port-field-name, handler-field-name>
     */
    protected void autoSubscribe(Map<String, String> portHandler) {
        autoSubscribed = true;

        List<Field> allFields = new ArrayList<Field>();

        Class currentClass = getClass();
        do {

            Field[] fields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);


        Field[] fieldsAsArray = allFields.toArray(new Field[allFields.size()]);
        List<PortReflection> ports = getPorts(fieldsAsArray);
        List<HandlerReflection> handlers = getHandlers(fieldsAsArray);

        Map<Field, Field> portHandlerMap = new HashMap<Field, Field>();
        for (String port : portHandler.keySet()) {
            String handler = portHandler.get(port);
            try {
                Field p = getClass().getDeclaredField(port);
                Field h = getClass().getDeclaredField(handler);
                portHandlerMap.put(p, h);
            } catch (NoSuchFieldException ex) {
                java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            } catch (SecurityException ex) {
                java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        for (Field portField : portHandlerMap.keySet()) {
            Field handlerField = portHandlerMap.get(portField);
            PortReflection portStruct = getPort(portField);
            HandlerReflection handlerStruct = getHandler(handlerField);

            // remove the handler from the list of handlers that will be
            // explicity subscribed
            handlers.remove(handlerStruct);
            subscribeHandlerToPort(portStruct, handlerStruct);
        }

        for (HandlerReflection handler : handlers) {
            if (subscribeHandlerToPorts(ports, handler) == false) {
                StringBuilder sb = new StringBuilder();
                for (PortReflection rf : ports) {
                    sb.append(rf.getPortType()).append(" ");
                }
                throw new IllegalStateException("Event Polarity Error? Couldn't subscribe handler of event type "
                        + handler.getEventType().getCanonicalName()
                        + " to any of the ports "
                        + sb.toString());
            }
        }
    }

    boolean subscribe(Class<? extends PortType> port, Field portField,
            Class<? extends Event> event, Field handler) {
        Class portParent = port.getSuperclass();

        portField.setAccessible(true);
        try {

            Method hasPositive = portParent.getDeclaredMethod(HAS_POSITIVE, Class.class);
            Method hasNegative = portParent.getDeclaredMethod(HAS_NEGATIVE, Class.class);


            // This class needs to be in se.sics.kompics package to have authorization
            // to use the PortCore object.
            PortCore pc = (PortCore) portField.get(this);
            if (pc == null) {
                return false;
            }
            Field portTypeObj = pc.getClass().getDeclaredField("portType");
            if (portTypeObj == null) {
                return false;
            }
            portTypeObj.setAccessible(true);
            PortType pt = (PortType) portTypeObj.get(pc);
            if (pt == null) {
                return false;
            }

            boolean res1 = (Boolean) hasNegative.invoke(pt, new Object[]{event});
            boolean res2 = (Boolean) hasPositive.invoke(pt, new Object[]{event});

            if (res1 == true || res2 == true) {
                handler.setAccessible(true);
                Handler h = (Handler) handler.get(this);
                subscribe(h, pc);
                return true;
            }
        } catch (NoSuchFieldException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    boolean subscribeHandlerToPorts(List<PortReflection> listPorts,
            HandlerReflection ht) {
        for (PortReflection p : listPorts) {
            if (subscribeHandlerToPort(p, ht) == true) {
                return true;
            }
        }
        return false;
    }

    boolean subscribeHandlerToPort(PortReflection portStruct,
            HandlerReflection ht) {

        Field handler = ht.getHandler();
        Class<? extends Event> event = ht.getEventType();

        Class<? extends PortType> port = (Class<? extends PortType>)
                portStruct.getPortType();

        if (subscribe(port, portStruct.getPortField(), event, handler) == true) {
            return true;
        }

        return false;
    }

    private List<Field> getPortFields(Field[] fields) {
        List<Field> ports =
                new ArrayList<Field>();
        for (Field f : fields) {
            try {
                f.setAccessible(true);
                if (f.getType().isAssignableFrom(Negative.class) || f.getType().isAssignableFrom(Positive.class)) {
                    ports.add(f);
                }
            } catch (IllegalArgumentException ex) {
                java.util.logging.Logger.getLogger(AutoSubscribeComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return ports;
    }

    private PortReflection getPort(Field field) {
        PortReflection port = null;
        Type genericFieldType = field.getGenericType();

        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = pt.getActualTypeArguments();
            for (Type fieldArgType : fieldArgTypes) {
                Class portClass = (Class) fieldArgType;
                port = new PortReflection(portClass, field);
            }
        }
        return port;
    }

    private List<PortReflection> getPorts(Field[] fields) {
        List<PortReflection> portE = new ArrayList<PortReflection>();
        List<Field> ports = getPortFields(fields);

        for (Field f : ports) {
            PortReflection pr = getPort(f);
            if (pr != null) {
                portE.add(pr);
            } else {
                throw new IllegalArgumentException("Invalid port supplied");
            }
        }
        return portE;
    }

    private List<Field> getHandlerFields(Field[] fields) {
        List<Field> handlers =
                new ArrayList<Field>();
        for (Field f : fields) {
            if (f.getType().isAssignableFrom(Handler.class)) {
                handlers.add(f);
            }
        }
        return handlers;
    }

    private HandlerReflection getHandler(Field field) {
        HandlerReflection handler = null;
        Type genericFieldType = field.getGenericType();

        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = pt.getActualTypeArguments();
            // A handler has only 1 generic type parameter - the type of the Event
            Type fieldArgType = fieldArgTypes[0];
            Class eventClass = (Class) fieldArgType;
            handler = new HandlerReflection(eventClass, field);
        }
        return handler;
    }

    private List<HandlerReflection> getHandlers(Field[] fields) {
        List<Field> handlerFields = getHandlerFields(fields);
        List<HandlerReflection> handlers = new ArrayList<HandlerReflection>();

        for (Field f : handlerFields) {
            HandlerReflection hr = getHandler(f);
            if (hr == null) {
                throw new RuntimeException("Invalid handler object");
            }
            handlers.add(hr);
        }
        return handlers;

    }
}
