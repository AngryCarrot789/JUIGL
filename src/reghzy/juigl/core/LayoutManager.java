package reghzy.juigl.core;

import reghzy.juigl.core.dispatcher.DispatchPriority;
import reghzy.juigl.core.dispatcher.Dispatcher;
import reghzy.juigl.core.ui.UIComponent;

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
        Dispatcher.getDispatcher().invokeAsync(o -> {
            o.updateLayout();
            return null;
        }, this, DispatchPriority.Render);
    }

    public void updateLayout() {
        UIComponent.FORCE_LAYOUT_COUNT++;
        for (UIComponent next : this.measureQueue.items) {
            if (!next.isMeasureDirty())
                continue;

            UIComponent lastDirty = null;
            UIComponent scanParent = next.getParent();
            while (scanParent != null) {
                if (scanParent.isMeasureDirty())
                    lastDirty = scanParent;
                scanParent = scanParent.getParent();
            }

            UIComponent target = lastDirty != null ? lastDirty : next;
            target.measure(target.getLastMeasureConstraintWidth(), target.getLastMeasureConstraintHeight());
        }

        for (UIComponent next : this.arrangeQueue.items) {
            if (!next.isArrangeDirty())
                continue;

            UIComponent lastDirty = null;
            UIComponent scanParent = next.getParent();
            while (scanParent != null) {
                if (scanParent.isArrangeDirty())
                    lastDirty = scanParent;
                scanParent = scanParent.getParent();
            }

            UIComponent target = lastDirty != null ? lastDirty : next;
            double w = target.getLastMeasureConstraintWidth() == Double.POSITIVE_INFINITY ? target.getDesiredWidth() : target.getLastArrangeWidth();
            double h = target.getLastMeasureConstraintHeight() == Double.POSITIVE_INFINITY ? target.getDesiredHeight() : target.getLastArrangeHeight();
            target.arrange(0, 0, w, h);
        }

        this.measureQueue.clear();
        this.arrangeQueue.clear();

        UIComponent.FORCE_LAYOUT_COUNT--;
        this.isLayoutRequested = false;
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

        void clear() {
            this.items.clear();
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
