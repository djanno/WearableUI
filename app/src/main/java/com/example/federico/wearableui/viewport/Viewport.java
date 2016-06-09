package com.example.federico.wearableui.viewport;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.federico.wearableui.controller.ViewportActivity;
import com.example.federico.wearableui.viewport.drawable_content.DrawableBitmap;
import com.example.federico.wearableui.viewport.drawable_content.DrawableCircle;
import com.example.federico.wearableui.viewport.drawable_content.DrawableContent;
import com.example.federico.wearableui.viewport.drawable_content.DrawableLine;
import com.example.federico.wearableui.viewport.drawable_content.DrawablePoint;
import com.example.federico.wearableui.viewport.drawable_content.DrawableRectangle;
import com.example.federico.wearableui.viewport.drawable_content.DrawableText;
import com.example.federico.wearableui.viewport.drawable_content.cursor.Cursor;
import com.example.federico.wearableui.viewport.drawable_content.cursor.ICursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 14/04/2016.
 */

/**
 * A Viewport is a custom view that has width and height equivalent or bigger than the screen in which
 * such view is displayed. This view is the only View that a {@link ViewportActivity}
 * can use as its content view. It has the ability to be navigated (or, to be "moved around") through
 * its scroll() method, which takes as argument the orientation of the wearer's gaze.
 * From the framework's point of view, a Viewport is basically a 2D blackboard that can be scrolled by
 * the movement of the user's head.
 * It's also important to know that a Viewport has a redefined coordinate system from a user's perspective:
 * the center of the Viewport is (0, 0), the top left corner is (-width / 2, height / 2), the top right is
 * (width / 2, height / 2), the bottom left is (-width / 2, -height / 2), the bottom right is (width / 2, -height / 2)
 */
public class Viewport extends View implements IViewport, View.OnTouchListener {

    private static final int Y_SCROLLING_ROM = 90;
    private static final int X_SCROLLING_ROM = 120;

    protected final Point screen;

    private int extraWidth;
    private int extraHeight;

    private int width;
    private int height;

    protected final ICursor cursor;
    protected final List<DrawableContent> children;
    protected final FrameLayout.LayoutParams params;

    private boolean locked;

    /* remaps a point expressed in the android coordinate system to a point expressed in the viewport coordinate system */
    private Point toViewportCoordinates(final Point androidCoordinate) {
        return(new Point(androidCoordinate.x - this.width / 2, this.height / 2 - androidCoordinate.y));
    }

    /* prepares a pain object with the given parameters */
    private Paint preparePaint(final int color, final int alpha, final boolean fill) {
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
        return paint;
    }

    /* constructs a viewport with the same dimension of the screen */
    public Viewport(final Context context) {
        this(context, 0f);
    }

    /* constructs a viewport with the given extra size compared to the screen dimension */
    public Viewport(final Context context, final float extraSize) {
        this(context, extraSize, extraSize);
    }

    /* constructs a viewport with the given extra width and height compared to the screen dimension */
    public Viewport(final Context context, final float extraWidth, final float extraHeight) {
        super(context);

        //by definition, a viewport can only be as big or bigger than the device screen
        if(extraWidth < 0 || extraHeight < 0) {
            throw new IllegalArgumentException("A Viewport can only be as big or bigger than the device screen." +
                    "You can not instantiate a Viewport with a negative extraWidth or extraHeight.");
        }

        //first we recover the screen size of the device and store it in a variable
        this.screen = new Point();
        final WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(this.screen);

        //the screen size that we recovered though, does not keep the height of the action bar into account.
        //since a viewport activity for which the viewport is used never has an action bar, we have to calculate
        //the height of the action bar manually, and add it to the screen size we previously calculated.
        //keep in mind that this calculus is not 100% accurate, the action bar height is always over-estimated by
        //a few pixels.
        TypedValue tv = new TypedValue();
        if (this.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            final int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            this.screen.y += actionBarHeight;
        }

        //initialize the cursor of the viewport
        this.cursor = new Cursor(this, this.preparePaint(Color.WHITE, 255, true), this.screen);

        //initialize the array that will store all the content of the viewport (a.k.a its children)
        this.children = new ArrayList<>();

        //calculate the extra width and height compared to the device screen in pixels
        this.extraWidth = (int) (extraWidth * this.screen.x);
        this.extraHeight = (int) (extraHeight * this.screen.y);

        //calculate the height and width of the viewport based on the extra dimensions that were passed as arguments
        this.width = this.screen.x + this.extraWidth;
        this.height = this.screen.y + this.extraHeight;

        //the viewport is initialized as locked
        this.locked = true;

        //calculate the margin so that the viewport appears centered on the screen upon creation
        //set the background color as black to get a better see-through effect, then set the on touch listener
        this.params = new FrameLayout.LayoutParams(this.width, this.height);
        this.params.leftMargin = -(this.extraWidth / 2);
        this.params.topMargin = -(this.extraHeight / 2);
        this.setLayoutParams(params);
        this.setBackgroundColor(Color.BLACK);
        this.setOnTouchListener(this);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        //every time the viewport is invalidated and redraws itself, it also informs its children to redraw themselves.
        //the children know how to draw themselves given the canvas of the parent
        for(final DrawableContent child : this.children) {
            child.drawOnCanvas(canvas);
        }
        //the cursor is redrawn as well, since it's a DrawableContent
        this.cursor.drawOnCanvas(canvas);
    }

    @Override
    public FieldOfView getFOV() {
        return new FieldOfView(this.toViewportCoordinates(new Point(-this.params.leftMargin, -this.params.topMargin)));
    }

    @Override
    public ICursor getCursor() {
        return this.cursor;
    }

    @Override
    public int getExtraWidth() {
        return this.extraWidth;
    }

    @Override
    public int getExtraHeight() {
        return this.extraHeight;
    }

    @Override
    public int getViewportWidth() {
        return this.width;
    }

    @Override
    public int getViewportHeight() {
        return this.height;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public DrawableText drawText(final Point where, final String text, final int size, final int color,
                         final int alpha, final boolean fill) {
        final Paint paint = this.preparePaint(color, alpha, fill);
        paint.setTextSize(size);
        final DrawableText toDraw = new DrawableText(where, this, paint, text);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public DrawableRectangle drawRectangle(final Point where, final int width, final int height, final int color,
                                           final int alpha, final boolean fill) {
        final Paint paint = this.preparePaint(color, alpha, fill);
        final DrawableRectangle toDraw = new DrawableRectangle(where, this, paint, width, height);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public DrawableCircle drawCircle(final Point where, final int radius, final int color, final int alpha,
                                     final boolean fill) {
        final Paint paint = this.preparePaint(color, alpha, fill);
        final DrawableCircle toDraw = new DrawableCircle(where, this, paint, radius);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public DrawableLine drawLine(final Point from, final Point to, final int color, final int alpha,
                                 final boolean fill) {
        final Paint paint = this.preparePaint(color, alpha, fill);
        final DrawableLine toDraw = new DrawableLine(from, to, this, paint);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public DrawablePoint drawPoint(final Point point, final int color, final int alpha, final boolean fill) {
        final Paint paint = this.preparePaint(color, alpha, fill);
        final DrawablePoint toDraw = new DrawablePoint(point, this, paint);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public DrawableBitmap drawImage(final int resId, final Point where, final int width, final int height) {
        final Resources res = this.getContext().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        final DrawableBitmap toDraw = new DrawableBitmap(where, this, null, bitmap);
        this.addContent(toDraw);
        return toDraw;
    }

    @Override
    public void addContent(final DrawableContent toAdd) {
        this.children.add(toAdd);
    }

    @Override
    public boolean removeContent(final DrawableContent toRemove) {
        if(this.children.remove(toRemove)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            //whenever a touch event is registered on the viewport, n event dispatcher task is executed
            //notice how the task works on a copy of the children list to avoid concurrent modification exceptions
            //this is not the best solution - the best solution would be to synchronize code
            final AsyncTask<Void, DrawableContent, Void> eventDispatcher = new AsyncTask<Void, DrawableContent, Void>() {

                private List<DrawableContent> safeCopy = new ArrayList<>();

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    this.safeCopy.addAll(Viewport.this.children);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    //the touch event coordinates are stored in a variable, then are remapped to the
                    //viewport coordinate system. However, if the touch event was dispatched by an
                    //actual touch event, the coordinate that are passed here are relative to the screen
                    //of the device and not to the actual view in which the event happened. If that's
                    //the case we have to remap those coordinates and making them relative to the view,
                    //by adding the current margins to it. If the touch event was dispatched
                    //programmatically instead (which means it comes from the Finger Source), there is no
                    //need to do that as the coordinates are already relative to the view itself.
                    //Once we have the event coordinates relative to the view, we can remap those
                    //coordinates to the viewport coordinate system. Once that is done, the thread
                    //checks if that event happened inside the bounds of a child of the viewport and,
                    //if that's the case, it informs the child by calling its fireEvent() method.
                    //notice how the list of children is cycled backwards and how the cycle stops as
                    //soon as one child whose bounds contain the event is found.
                    //This means that if there are more children whose bounds overlap and the event is
                    //fired between those children's bounds, only the one with the greatest Z coordinate
                    //will be registering the event.
                    for(int i = this.safeCopy.size() - 1; i >= 0; i--) {
                        final Point point = new Point((int) event.getX(), (int) event.getY());
                        //the click made by the Finger Source, is taken as a shift+click and doesn't need
                        //the following mapping
                        if(event.getMetaState() != KeyEvent.META_SHIFT_ON) {
                            point.x -= Viewport.this.params.leftMargin;
                            point.y -= Viewport.this.params.topMargin;
                        }
                        point.x -= Viewport.this.width / 2;
                        point.y = Viewport.this.height / 2 - point.y;
                        if(this.safeCopy.get(i).isInBounds(point)) {
                            this.publishProgress(this.safeCopy.get(i));
                            return null;
                        }
                    }
                    return null;
                }

                //if a child whose bounds contain the event is found and that child had an active listener
                //to handle the event, the viewport is invalidated and, therefore, so are all of its children
                @Override
                protected void onProgressUpdate(DrawableContent... values) {
                    super.onProgressUpdate(values);
                    //the event is fired on the main thread just in case it has to update the view
                    if(values[0].fireEvent()) {
                        Viewport.this.invalidate();
                    }
                }
            };

            eventDispatcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        return true;
    }

    @Override
    public void scrollAccordingly(final float pitch, final float yaw) {
        //if the viewport isn't locked, begin scrolling procedure
        if(!this.locked) {
            final int oldTopMargin = this.params.topMargin;
            final int oldLeftMargin = this.params.leftMargin;
            //compute the new margins that will be used to scroll the viewport from the values of pitch and yaw passed to the method.
            //keep in mind that these values represent the pitch and yaw from our zero point, which we obtained during the calibration phase
            //i.e. a pitch of 30 deg means 30 deg above the zero point, a yaw of 30 deg means 30 deg to the left of our zero point

            //-extraHeight / 2 and -extraWidth / 2 are the margin values that we need to have our field of view in the center of the viewport
            //to these we sum the current pitch multiplied by a coefficient to obtain the number of pixels for each degree
            //the coefficient was obtained from -> pitch : Y_SCROLLING_ROM / 2 = deltaTopMargin : extraHeight / 2
            this.params.topMargin = (int) ((-this.extraHeight / 2) + (pitch * this.extraHeight / Y_SCROLLING_ROM));
            this.params.leftMargin = (int) ((-this.extraWidth / 2) + (yaw * this.extraWidth / X_SCROLLING_ROM));
            //scroll the viewport by setting the margins
            this.setLayoutParams(this.params);
            //scroll the cursor accordingly, so that it's always inside the field of view
            final Point cursorCoordinates = this.cursor.getViewportCoordinates();
            cursorCoordinates.x -= (this.params.leftMargin - oldLeftMargin);
            cursorCoordinates.y += (this.params.topMargin - oldTopMargin);
            this.cursor.moveTo(cursorCoordinates);
        }
        this.invalidate();
    }

    @Override
    public void lock() {
        this.locked = true;
    }

    @Override
    public void unlock() {
        this.locked = false;
    }

    /**
     * Wrapper class used to express the dimension and position of the portion of the viewport that is
     * visible to the user. It uses the Viewport coordinate system.
     */
    public class FieldOfView {

        private final Rect fov;

        public FieldOfView(final Point topLeft) {
            this.fov = new Rect(topLeft.x, topLeft.y, topLeft.x + screen.x, topLeft.y - screen.y);
        }

        public Point getCenter() {
            return new Point(this.fov.left + screen.x / 2, this.fov.top - screen.y / 2);
        }
    }

}
