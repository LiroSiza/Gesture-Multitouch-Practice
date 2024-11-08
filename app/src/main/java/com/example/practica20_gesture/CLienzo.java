package com.example.practica20_gesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CLienzo extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    private final int SIZE = 80;
    // Matriz de punteros a objetos PonitF
    private HashMap<Integer,PointF> punterosActivos = new HashMap<Integer,PointF>();
    private Paint myPaint = new Paint();
    private int[]colors = {
            Color.BLUE, Color.GREEN, Color.MAGENTA, Color.BLACK, Color.CYAN,
            Color.GRAY, Color.RED, Color.LTGRAY, Color.YELLOW};
    private Paint textPaint = new Paint();
    //  Gesture
    private String txtGesture;
    private GestureDetector mDetector;

    // Lista de eventos
    private HashMap<String,Boolean> listaEventos = new HashMap<String,Boolean>();

    int time = 0;

    private float scaleFactor = 1f;        // Factor de escala para zoom
    private float rotationAngle = 0f;      // Ángulo de rotación
    private float translateX = 0f;         // Desplazamiento en X
    private float translateY = 0f;         // Desplazamiento en Y
    private ScaleGestureDetector scaleDetector;  // Detector de gestos de escala

    public CLienzo(Context context){
        super(context);
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40f);

        // Agregar posibles métodos a la lista
        desactivarGestos();

        txtGesture = "gesture actual";
        mDetector = new GestureDetector(context.getApplicationContext(), this);
        // Set the gesture detector as the double tap listener.
        mDetector.setOnDoubleTapListener(this);
        // Inicializar detectores de gestos
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public boolean onTouchEvent(MotionEvent event){
        scaleDetector.onTouchEvent(event);     // Procesa gestos de escala
        mDetector.onTouchEvent(event);         // Procesa gestos regulares

        int indice = event.getActionIndex();
        // obtenemos el puntero
        int apuntadorId = event.getPointerId(indice);
        // Obtiene el puntero relacionado
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
            // Agregamos el puntero a la lista
            PointF f = new PointF();
            f.x = event.getX(indice);
            f.y = event.getY(indice);
            punterosActivos.put(apuntadorId, f);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            // Puntero se movió
            int size = event.getPointerCount();
            int i = 0;
            while (i < size) {
                //int pointerId = event.getPointerId(i);
                PointF puntero = punterosActivos.get(event.getPointerId(i));
                if(puntero != null){
                    puntero.x = event.getX(i);
                    puntero.y = event.getY(i);
                }
                i++;
            }
        }
        if(event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_POINTER_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            punterosActivos.remove(apuntadorId);
            PointF f = new PointF();
            f.x = event.getX(indice);
            f.y = event.getY(indice);

            // Verifica si ya no hay punteros activos
            if (punterosActivos.isEmpty()) {
                desactivarGestos();  // Llama al método para desactivar los gestos
            }
        }
        invalidate();
        if(mDetector.onTouchEvent(event)){
            return true;
        }else{
            return super.onTouchEvent(event);
        }
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        // pintamos todos los punteros
        int size = punterosActivos.size();
        int i = 0;
        PointF puntero = new PointF();
        Iterator<Integer> itr = punterosActivos.keySet().iterator();

        while(itr.hasNext()){
            i=itr.next();
            puntero = punterosActivos.get(i);
            if(puntero != null){
                myPaint.setColor(colors[i % 9]);
            }
            canvas.drawCircle(puntero.x, puntero.y, SIZE, myPaint);
        }

        // Aplicación de transformaciones al canvas
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);
        canvas.rotate(rotationAngle, getWidth() / 2f, getHeight() / 2f); // Rotación alrededor del centro

        canvas.drawText("Total punteros: " + punterosActivos.size(), 10f, 80f, textPaint);
        canvas.drawText(txtGesture, 10f, 120f, textPaint);

        canvas.drawText("onSingleTapConfirmed: " + listaEventos.get("onSingleTapConfirmed").toString(), 10f, 170f, textPaint);
        canvas.drawText("onDoubleTapEvent: " + listaEventos.get("onDoubleTapConfirmed").toString(), 10f, 210f, textPaint);
        canvas.drawText("onDown: " + listaEventos.get("onDown").toString(), 10f, 250f, textPaint);
        canvas.drawText("onLongPress: " + listaEventos.get("onLongPress").toString(), 10f, 290f, textPaint);
        canvas.drawText("onFling: " + listaEventos.get("onFling").toString(), 10f, 330f, textPaint);

        canvas.drawText("onDoubleTap: " + listaEventos.get("onSingleTapConfirmed").toString(), 600f, 170f, textPaint);
        canvas.drawText("onShowPress: " + listaEventos.get("onDoubleTapConfirmed").toString(), 600f, 210f, textPaint);
        canvas.drawText("onSingletapUp: " + listaEventos.get("onDown").toString(), 600f, 250f, textPaint);
        canvas.drawText("onScroll: " + listaEventos.get("onScroll").toString(), 600f, 290f, textPaint);
    }

    // Clase interna para manejar gestos de escala (acercar/alejar)
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Limita el factor de escala
            invalidate();
            return true;
        }
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent motionEvent) {
        txtGesture = "onSingleTapConfirmed ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onSingleTapConfirmed",true);
        checkEventsList();
        return true;
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent motionEvent) {
        txtGesture = "onDoubleTap ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onDoubleTap",true);
        checkEventsList();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(@NonNull MotionEvent motionEvent) {
        txtGesture = "onDoubleTapEvent ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onDoubleTapConfirmed",true);
        checkEventsList();
        return true;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        txtGesture = "onDown ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onDown",true);
        checkEventsList();
        return true;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {
        txtGesture = "onShowPress ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onShowPress",true);
        checkEventsList();
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        txtGesture = "onSingleTapUp ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onSingletapUp",true);
        checkEventsList();
        return true;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        txtGesture = "onScroll ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onScroll",true);
        checkEventsList();
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {
        txtGesture = "onLongPress ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onLongPress",true);
        checkEventsList();
        invalidate();
    }

    @Override
    public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        txtGesture = "onFling ";
        txtGesture+=motionEvent.toString();
        // Activar o desactivar los eventos que se afectan
        listaEventos.put("onFling",true);
        checkEventsList();
        return true;
    }

    public void checkEventsList(){
        if (listaEventos.get("onDoubleTapConfirmed")) {
            listaEventos.put("onSingleTapConfirmed", false);
        }

        if (listaEventos.get("onSingleTapConfirmed")) {
            listaEventos.put("onDoubleTap", false);
            listaEventos.put("onScroll", false);
        }

        if (listaEventos.get("onFling")) {
            listaEventos.put("onScroll", false);
            listaEventos.put("onSingleTapConfirmed", false);
        } else if (listaEventos.get("onScroll")) {
            listaEventos.put("onFling", false);
        }

        if (listaEventos.get("onLongPress")) {
            listaEventos.put("onSingleTapConfirmed", false);
            listaEventos.put("onDoubleTap", false);
            listaEventos.put("onScroll", false);
            listaEventos.put("onFling", false);
        }

        if (listaEventos.get("onShowPress")) {
            listaEventos.put("onScroll", false);
            listaEventos.put("onSingleTapConfirmed", false);
        }
    }
    public void desactivarGestos(){
        listaEventos.put("onSingleTapConfirmed",false);
        listaEventos.put("onDoubleTapConfirmed",false);
        listaEventos.put("onDoubleTap",false);
        listaEventos.put("onDown",false);
        listaEventos.put("onShowPress",false);
        listaEventos.put("onSingletapUp",false);
        listaEventos.put("onScroll",false);
        listaEventos.put("onLongPress",false);
        listaEventos.put("onFling",false);
    }
}
