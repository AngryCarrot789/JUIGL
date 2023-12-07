package reghzy.juigl.core.dependency;

public interface PropertyChangedHandler {
    void onPropertyChanged(DependencyObject owner, DependencyProperty property, Object oldValue, Object newValue);
}
