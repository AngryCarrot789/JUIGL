package reghzy.juigl;

import reghzy.juigl.core.dependency.DependencyObject;
import reghzy.juigl.core.dependency.DependencyProperty;
import reghzy.juigl.core.dependency.PropertyMeta;

public class MyButton extends DependencyObject {
    public static final DependencyProperty TextProperty = DependencyProperty.register("text", String.class, DependencyObject.class, new PropertyMeta("joesss!"));
}
