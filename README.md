# JUIGL
A java UI framework, very similar to WPF (featuring a property system, component alignment as left/top, right/bottom, center and stretch, margins, etc.)

Very WIP at the moment; Windows are not UI objects at the moment, however, measurement/arrangement/rendering works

## IntelliJ IDEA helpers
You can create a live template to auto define new properties:
```
public static final DependencyProperty $PROPERTYNAME$Property = DependencyProperty.register("$PROPERTYNAME$", $TYPE$.class, $CURRENT_CLASS$.class, new PropertyMeta(null));
public $TYPE$ get$PROPERTYNAME$() { return ($TYPE$) getValue($PROPERTYNAME$Property); }
public void set$PROPERTYNAME$($TYPE$ value) { setValue($PROPERTYNAME$Property, value); }
```
Then select `Edit variables` and set the expression for `CURRENT_CLASS` to `className()`
