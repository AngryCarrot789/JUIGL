package reghzy.juigl.core;

import reghzy.juigl.Main;
import reghzy.juigl.core.dispatcher.DispatchPriority;
import reghzy.juigl.core.dispatcher.Dispatcher;
import reghzy.juigl.core.ui.UIComponent;
import reghzy.juigl.utils.Maths;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;

public class LayoutManager {
    public final LayoutQueue measureQueue;
    public final LayoutQueue arrangeQueue;
    private boolean isLayoutRequested;

    public LayoutManager() {
        this.measureQueue = new MeasureLayoutQueue(this);
        this.arrangeQueue = new ArrangeLayoutQueue(this);
    }

    public static LayoutManager getLayoutManager() {
        return getLayoutManager(Dispatcher.getDispatcher());
    }

    // lazy but fast way to access a layout manager for a thread. could make it static....?
    public static LayoutManager getLayoutManager(Dispatcher dispatcher) {
        Object value = dispatcher.reserved0;
        if (!(value instanceof LayoutManager))
            dispatcher.reserved0 = value = new LayoutManager();
        return (LayoutManager) value;
    }

    public void requestUpdateLayout() {
        if (this.isLayoutRequested) {
            return;
        }

        this.isLayoutRequested = true;
        Dispatcher.getDispatcher().invokeLater(o -> {
            o.updateLayout();
            this.isLayoutRequested = false;
            return null;
        }, this, DispatchPriority.Render);
    }

    public void updateLayout() {
        UIComponent.FORCE_LAYOUT_COUNT++;
        for (UIComponent next : this.measureQueue.items) {
            UIComponent lastDirty = null;
            UIComponent scanParent = next.getParent();
            while (scanParent != null) {
                if (scanParent.isMeasureDirty())
                    lastDirty = scanParent;
                scanParent = scanParent.getParent();
            }

            double w,h;
            UIComponent target = lastDirty != null ? lastDirty : next;
            UIComponent parent;
            if (!target.hasNeverMeasured()) {
                w = target.getLastMeasureConstraintWidth();
                h = target.getLastMeasureConstraintHeight();
            }
            else if ((parent = target.getParent()) == null) {
                w = h = Double.POSITIVE_INFINITY;
            }
            else {
                if (parent.hasNeverMeasured())
                    throw new RuntimeException("Layout fatal error. Expected parent object to be measured");
                w = parent.getLastMeasureConstraintWidth();
                h = parent.getLastMeasureConstraintHeight();
            }

            double lastDw = target.getDesiredWidth(),lastDh = target.getDesiredHeight();
            target.measure(w, h);
            if (!Maths.equals(target.getDesiredWidth(), lastDw) || !Maths.equals(target.getDesiredHeight(), lastDh)) {
                target.invalidateArrange();
            }
        }

        ArrayList<UIComponent> draw = new ArrayList<>();

        for (UIComponent next : this.arrangeQueue.items) {
            UIComponent lastDirty = null;
            UIComponent scanParent = next.getParent();
            while (scanParent != null) {
                if (scanParent.isArrangeDirty())
                    lastDirty = scanParent;
                scanParent = scanParent.getParent();
            }

            double x, y, w, h;
            UIComponent target = lastDirty != null ? lastDirty : next;
            UIComponent parent;
            if (!target.hasNeverArranged()) {
                x = target.getLastArrangePosX();
                y = target.getLastArrangePosY();
                w = target.getLastArrangeWidth();
                h = target.getLastArrangeHeight();
            }
            else if ((parent = target.getParent()) == null) {
                Window window = target.getOwnerWindow();
                if (window == null) {
                    x = y = w = h = 0;
                }
                else {
                    x = y = 0;
                    w = window.getWidth();
                    h = window.getHeight();
                }
            }
            else {
                if (parent.hasNeverArranged())
                    throw new RuntimeException("Layout fatal error. Expected parent object to be arranged");
                x = y = 0;
                w = parent.getLastArrangeWidth();
                h = parent.getLastArrangeHeight();
            }

            double lastArrX = target.getLastArrangePosX();
            double lastArrY = target.getLastArrangePosY();
            double lastArrW = target.getLastArrangeWidth();
            double lastArrH = target.getLastArrangeHeight();
            target.arrange(x, y, w, h);
            if (target.isVisualDirty() || !Maths.equals(target.getLastArrangePosX(), lastArrX) || !Maths.equals(target.getLastArrangePosY(), lastArrY) || !Maths.equals(target.getLastArrangeWidth(), lastArrW) || !Maths.equals(target.getLastArrangeHeight(), lastArrH)) {
                draw.add(target);
            }
        }

        for (UIComponent component : draw) {
            component.doRenderComponent();
        }

        UIComponent.FORCE_LAYOUT_COUNT--;
        Main.mainWindow.draw();
    }

    public static abstract class LayoutQueue {
        private final ArrayList<UIComponent> items;
        private final LayoutManager manager;

        protected LayoutQueue(LayoutManager layoutManager) {
            this.items = new ArrayList<>();
            this.manager = layoutManager;
        }

        protected abstract boolean isInQueue(UIComponent component);

        public LayoutManager getManager() {
            return this.manager;
        }

        public void add(UIComponent component) {
            if (!this.isInQueue(component))
                this.items.add(component);
            this.manager.requestUpdateLayout();
        }

        public boolean remove(UIComponent component) {
            return this.items.remove(component);
        }
    }

    private static class MeasureLayoutQueue extends LayoutQueue {
        protected MeasureLayoutQueue(LayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        protected boolean isInQueue(UIComponent component) {
            return component.isMeasureDirty();
        }
    }

    private static class ArrangeLayoutQueue extends LayoutQueue {
        protected ArrangeLayoutQueue(LayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        protected boolean isInQueue(UIComponent component) {
            return component.isArrangeDirty();
        }
    }
}
