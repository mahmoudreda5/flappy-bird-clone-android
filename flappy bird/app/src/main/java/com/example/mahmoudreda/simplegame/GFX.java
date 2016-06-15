package com.example.mahmoudreda.simplegame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

/**
 * Created by Mahmoud Reda on 3/19/2016.
 */
public class GFX extends Activity implements View.OnTouchListener{

    final int GRAVITY = 2, WIDTHCONST = 7, HEIGHTCONST = 14, SLEEPINGTIME = 20, FLYINGUP = -20
            , TUBEVELOCITY = 4, NUMOFTUBES = 4, TEXTSIZE = 70;

    MyView myView;
    Bitmap background, topTube, bottomTube, gameOver;
    Bitmap [] bird;
    Rect bgrect, birdrect, topTuberect, bottomTuberect, gameOverrect;
    Rect [] topCollision;
    Rect [] bottomCollision;
    Paint textPaint;

    float gap;
    float [] tubeX, tubeOffsets;
    float birdX, birdY, birdVelocity, canvasWidth, canvasHeight, distBetweenTubes;
    float maxTubeOffset;

    int birdState, gameState;
    int score, scoringTube;
    boolean istouched, gameOverState;

    Random randomGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize our game view
        myView = new MyView(getApplicationContext());
        setContentView(myView);


        //initialize the game background, tubes, bird, and the game over
        background = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        topTube = BitmapFactory.decodeResource(getResources(), R.drawable.toptube);
        bottomTube = BitmapFactory.decodeResource(getResources(), R.drawable.bottomtube);
        bird = new Bitmap[2];
        bird[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        bird[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bird2);
        gameOver = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);



        //initialize the scaling rectangles
        bgrect = new Rect();
        topTuberect = new Rect();
        bottomTuberect = new Rect();
        birdrect = new Rect();
        gameOverrect = new Rect();

        //initialize the paint
        textPaint = new Paint();

        //initialize the collision rect arrays
        topCollision = new Rect[NUMOFTUBES];
        bottomCollision = new Rect[NUMOFTUBES];

        //initialize the tubes offsets and x position
        tubeX = new float[NUMOFTUBES];
        tubeOffsets = new float[NUMOFTUBES];

        gap = 0;
        birdState = 0;
        birdX = birdY = birdVelocity = canvasWidth = canvasHeight = distBetweenTubes = score = scoringTube = 0;
        maxTubeOffset = 0;

        //initialize the random generator
        randomGenerator = new Random();

        istouched = gameOverState = false;
        gameState = 0;

        //when the user touch the screen (canvas)
        myView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //start a new game after the bird death
                if(gameOverState) gameState = 0;
                else
                //the user started the game
                gameState = 1;

                //the user touched the screen (canvas)
                istouched = true;

                //return false for just touch once
                return false;
            }
        });
    }

    //on the activity pause
    @Override
    protected void onPause() {
        super.onPause();

        //pause the canvas
        myView.pause();
    }

    //on the activity resume
    @Override
    protected void onResume() {
        super.onResume();

        //resume the canvas
        myView.resume();
    }

    //when the user touch the screen (canvas)
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //return false for just touch once
        return false;
    }

    //my view inner class, deals with drawing the game graphics/////////////////////////////////////
    public class MyView extends SurfaceView implements Runnable{


        Canvas canvas;
        SurfaceHolder surfaceHolder;
        Thread thread = null;

        boolean isRunning = true;

        //my view class constructor
        public MyView(Context context) {
            super(context);

            //initialize the surface holder and the thread
            surfaceHolder = getHolder();
            thread = new Thread(this);

            //start the graphics thread
            thread.start();
        }

        //pause the canvas when the activity is paused
        public void pause(){
            isRunning = false;

            try {
                //close the graphics thread
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            thread = null;
        }

        //resume the canvas when the activity is resumed
        public void resume(){
            isRunning = true;

            //initialize the graphics thread and run it again
            thread = new Thread(this);
            thread.start();
        }

        //the run method, deals with the canvas drawing
        @Override
        public void run() {

            //while the thread is active (the activity is resumed)
            while(isRunning) {

                //if the surface is not valid
                if (!surfaceHolder.getSurface().isValid()) continue;

                    //switch between the bird states
                    if (birdState == 0) birdState = 1;
                    else birdState = 0;

                    //lock the canvas and start draw
                     canvas = surfaceHolder.lockCanvas();

                    //the background scaling rectangle
                    bgrect.set(0, 0, canvas.getWidth(), canvas.getHeight());
                    //draw the game background
                    canvas.drawBitmap(background, null, bgrect, null);

                //if the game not started yet
                if(gameState == 0){

                    //get the canvas width and height (for less processing)
                    canvasWidth = canvas.getWidth();
                    canvasHeight = canvas.getHeight();

                    //initialize the Gap between the tubes dependent on the canvas height
                    gap = canvasHeight / 2;

                    //initialize the maximum tube offset
                    maxTubeOffset = canvasHeight / 2 - gap / 2 - 30;

                    //initialize the bird position to the canvas center
                    birdX = canvasWidth / 2;
                    birdY = canvasHeight / 2;

                    //set the distance between tubes
                    distBetweenTubes = canvasWidth * 3 / 4;

                    //set the tubes offset and x position
                    for (int i = 0; i < NUMOFTUBES; i++){
                        //set the ith tube offset
                        tubeOffsets[i] = (randomGenerator.nextFloat() - 0.5f) * 2 * maxTubeOffset;

                        //set the ith tube x position
                        tubeX[i] = canvasWidth / 2 + canvasWidth + i * distBetweenTubes;

                        ////initialize the collision rect arrays
                        topCollision[i] = new Rect();
                        bottomCollision[i] = new Rect();
                    }

                    //set the paint properties
                    textPaint.setColor(Color.BLUE);
                    textPaint.setTextSize(TEXTSIZE);

                    //reset the bird velocity, score, score tube, and the game over state for the new game
                    birdVelocity = score = scoringTube = 0;
                    gameOverState = false;

                }
                //the user started the game
                else if(gameState == 1){

                    //check the scoring tube position to increment the score (when the right of the tube < the left of the bird)
                    if(tubeX[scoringTube] + canvasWidth / WIDTHCONST - canvasWidth / WIDTHCONST / 2 <
                            birdX - canvasWidth / WIDTHCONST / 2) {
                        //increment the score by one
                        score++;

                        //increment and reset the scoring tube
                        scoringTube++;
                        if(scoringTube == NUMOFTUBES) scoringTube = 0;
                    }


                    for(int i = 0; i < NUMOFTUBES; i++) {

                        //if the tube reached the end of the queue
                        if(tubeX[i] < - canvasWidth / WIDTHCONST) {
                            //make the tube jump the start of the queue (reset ith tube x position)
                            tubeX[i] = NUMOFTUBES * distBetweenTubes;

                            //reset ith tube offset
                            tubeOffsets[i] = (randomGenerator.nextFloat() - 0.5f) * 2 * maxTubeOffset;
                        }

                        tubeX[i] -= TUBEVELOCITY;

                        /*
                        //check the tube position to increment the score (when the right of the tube < the left of the bird)
                        if(tubeX[i] + canvasWidth / WIDTHCONST - canvasWidth / WIDTHCONST / 2 <
                                birdX - (int) canvasWidth / WIDTHCONST / 2)
                            //increment the score by one
                                score++;
                        */

                        //draw the tubes
                        //tob tube scaling rectangle
                        topTuberect.set((int) tubeX[i] - (int) canvasWidth / WIDTHCONST / 2, 0,
                                (int) tubeX[i] + (int) canvasWidth / WIDTHCONST - (int) canvasWidth / WIDTHCONST / 2
                                , (int) canvasHeight / 2 - (int) gap / 4 + (int) tubeOffsets[i]);
                        //bottom tube scaling rectangle
                        bottomTuberect.set((int) tubeX[i] - (int) canvasWidth / WIDTHCONST / 2, (int) canvasHeight / 2 + (int) gap / 4 + (int) tubeOffsets[i],
                                (int) tubeX[i] + (int) canvasWidth / WIDTHCONST - (int) canvasWidth / WIDTHCONST / 2, (int) canvasHeight);

                        //save the Rect object to check the collision
                        topCollision[i].set(topTuberect);
                        bottomCollision[i].set(bottomTuberect);

                        //draw the tube on the canvas
                        canvas.drawBitmap(topTube, null, topTuberect, null);
                        canvas.drawBitmap(bottomTube, null, bottomTuberect, null);
                    }

                    //falling to the ground by the velocity when the canvas is not touched
                    //or going up when the canvas is touched
                    if(istouched)
                        birdVelocity = FLYINGUP;
                    else birdVelocity += GRAVITY;

                    //for not exceeding the canvas limits (top and bottom)
                    if(birdY + birdVelocity < canvas.getHeight()
                             && birdY + birdVelocity > 0)
                        birdY += birdVelocity;
                    else if(birdY + birdVelocity > canvasHeight)
                        //then the bird should die
                        gameState = 2;

                }
                //game over
                else if(gameState == 2){
                    //game over
                    gameOverState = true;

                        //drawing the tubes in the same place
                        for(int i = 0; i < NUMOFTUBES; i++) {
                            if (topCollision[i].right < canvasWidth && topCollision[i].left > 0) {
                                //draw top tube in the canvas in the same place
                                canvas.drawBitmap(topTube, null, topCollision[i], null);
                                canvas.drawBitmap(bottomTube, null, bottomCollision[i], null);
                            }
                        }

                        //draw the bird
                        //bird scaling rectangle
                        birdrect.set((int) birdX - (int) canvasWidth / WIDTHCONST / 2, (int) birdY - (int) canvasHeight / HEIGHTCONST / 2,
                                (int) birdX + (int) canvasWidth / WIDTHCONST - (int) canvasWidth / WIDTHCONST / 2,
                                (int) birdY + (int) canvasHeight / HEIGHTCONST - (int) canvasHeight / HEIGHTCONST / 2);

                        //draw the bird
                        canvas.drawBitmap(bird[birdState], null, birdrect, null);

                        //game over scaling rectangle
                        gameOverrect.set((int) canvasWidth / WIDTHCONST, (int) canvasHeight / 2 - gameOver.getHeight() / 2,
                                (int) canvasWidth - (int) canvasWidth / WIDTHCONST,
                                (int) canvasHeight / 2 - gameOver.getHeight() / 2 + (int) canvasHeight / HEIGHTCONST * 3 / 2);

                        //draw the game over
                        canvas.drawBitmap(gameOver, null, gameOverrect, null);
                    }

                    //draw the bird
                    //bird scaling rectangle
                    birdrect.set((int) birdX - (int) canvasWidth / WIDTHCONST / 2, (int) birdY - (int) canvasHeight / HEIGHTCONST / 2,
                            (int) birdX + (int) canvasWidth / WIDTHCONST - (int) canvasWidth / WIDTHCONST / 2,
                            (int) birdY + (int) canvasHeight / HEIGHTCONST - (int) canvasHeight / HEIGHTCONST / 2);

                    //draw the bird
                    canvas.drawBitmap(bird[birdState], null, birdrect, null);

                    //draw the score
                    canvas.drawText(String.valueOf(score), canvasWidth / WIDTHCONST / 2,
                            canvasHeight - canvasWidth / HEIGHTCONST, textPaint);

                    //collision detection
                    for(int i = 0; i < NUMOFTUBES && gameState == 1; i++){

                        //for tubes which is not on the canvas
                        if(topCollision[i].right < 0 || topCollision[i].left > canvasWidth) continue;

                        //check the intersection of the bird with the tubes on the canvas
                        //for the top tube
                        if(birdrect.right > topCollision[i].left && birdrect.left < topCollision[i].right
                                && birdrect.top < topCollision[i].bottom){
                            //collision happened
                            gameState = 2;
                            //Log.d("collision", "top collision");
                        }
                        //for the bottom tube
                        if(birdrect.right > bottomCollision[i].left && birdrect.left < bottomCollision[i].right
                                && birdrect.bottom > bottomCollision[i].top) {
                            //collision happened
                            gameState = 2;
                            //Log.d("collision", "bottom collision");
                        }

                    }

                    //unlock the canvas after drawing
                    surfaceHolder.unlockCanvasAndPost(canvas);

                    //reset is touched
                    istouched = false;


                    //sleeping
                    try {
                        Thread.sleep(SLEEPINGTIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
