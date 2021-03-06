package fillers.process;

import annotations.Fill;
import fillers.BaseFiller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by r.isin on 21.02.2017.
 *
 * Class for annotation process
 *
 * @author MiF
 * @version $Id: $Id
 */
public class Filler<T>{
    private Class<T> item;

    /**
     * <p>of.</p>
     *
     * @param type a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a {@link fillers.process.Filler} object.
     */
    public static <T> Filler<T> of(Class<T> type){
        return new Filler<T>(type);
    }

    private Filler(Class<T> item) {
        this.item = item;
    }

    /**
     * <p>single.</p>
     *
     * @return Single filled object of Class T
     * @throws java.lang.IllegalAccessException when class not accessible
     * @throws java.lang.InstantiationException when class doesn't have default constructor
     * @throws java.lang.NoSuchMethodException when filler class doesn't have default method marked in annotation
     * @throws java.lang.reflect.InvocationTargetException when filler method invocation not possible
     */
    public T single() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return items(1).get(0);
    }

    /**
     * <p>items.</p>
     *
     * @param count single of filled objects
     * @return List with count of elements
     * @throws java.lang.IllegalAccessException when class not accessible
     * @throws java.lang.InstantiationException when class doesn't have default constructor
     * @throws java.lang.NoSuchMethodException when filler class doesn't have default method marked in annotation
     * @throws java.lang.reflect.InvocationTargetException when filler method invocation not possible
     */
    public List<T> items(int count) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        final List<T> ret = new ArrayList<T>(count);
        T instance;
        for (int i = 0; i < count; i++) {
            instance = item.newInstance();
            List<Field> fields = Arrays.asList(instance.getClass().getDeclaredFields());
            fields = fields.stream().filter((field -> field.isAnnotationPresent(Fill.class))).collect(Collectors.toList());
            for(Field field : fields){
                field.setAccessible(true);
                setValue(instance, field);
            }
            ret.add(instance);
        }
        return ret;
    }

    private void setValue(Object instance, Field field) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Fill item = field.getAnnotation(Fill.class);
        Class<? extends BaseFiller> c = item.filler();
        Method m = Arrays.stream(c.getMethods()).filter((method) -> (
                method.getName().equals(item.method()) && method.getParameterCount() == item.params().length
                )).findFirst()
                .orElseThrow(() ->
                        new NoSuchMethodException(
                                c.getName() + "." + item.method() + "("
                                    + Arrays.stream(item.params()).map(x -> "\"" + x + "\"")
                                        .collect(Collectors.joining(", ")) + ")"
                        )
                );

        field.set(instance, m.invoke(c.newInstance(), (Object[]) item.params()));
    }

}
