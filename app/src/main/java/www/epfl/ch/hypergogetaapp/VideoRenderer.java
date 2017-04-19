package www.epfl.ch.hypergogetaapp;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Williamallas on 26.03.2017.
 */

public class VideoRenderer implements GLSurfaceView.Renderer {

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        init();
    }

    public void onDrawFrame(GL10 unused) {

        Bitmap toUpload = null;
        synchronized (_bitmaps) {
            toUpload = _bitmaps.poll();
        }

        if(toUpload != null){
            _glTextures.add(new int[1]);
            _glTextures.get(_glTextures.size()-1)[0] = uploadBitmap(toUpload);
        }

        if(_needClear){
            for(int[] tex : _glTextures){
                GLES20.glDeleteTextures(1, tex, 0);
            }
            _needClear = false;
            _glTextures.clear();
        }

        render();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void addFrame(Bitmap bitmap) {
        synchronized (_bitmaps) {
            _bitmaps.add(bitmap);
        }
    }

    public void clear() {
        synchronized (_bitmaps) {
            _bitmaps.clear();
            _needClear = true;
        }
    }

    public void setWindowSize(int size) {
        _windowSize = size;
    }

    private void init(){
        float[] uv = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f };

        float coef = 0.9f;
        float[] position = {
                -1.0f*coef, 1.0f*coef,
                1.0f*coef, 1.0f*coef,
                1.0f*coef, -1.0f*coef,
                -1.0f*coef, -1.0f*coef };

        short[] indexes = { 0,1,2, 2,3,0 };

        _uvBuffer = createBuffer(uv);
        _vertexBuffer = createBuffer(position);
        _indexBuffer = createBuffer(indexes);

        _renderVideoShader = createShaderProgram(vertexShader_src, pixelShader_src);

        GLES20.glDisable(GL10.GL_DEPTH_TEST);
        GLES20.glClearColor(0.6f, 0.6f, 0.6f, 1.0f);
    }

    private void render(){

        if(_glTextures.isEmpty()){
            //return;
        }
        else{
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _glTextures.get(_glTextures.size()-1)[0]);
        }

        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(_renderVideoShader);

        /*************************/
        /* Setup uniform         */
        /*************************/

        int samplerLoc = GLES20.glGetUniformLocation (_renderVideoShader, "texture[0]" );
        GLES20.glUniform1i ( samplerLoc, 0);

        /*************************/
        /* Setup attrib location */
        /*************************/

        int vertexAttribLocation = GLES20.glGetAttribLocation(_renderVideoShader, "vertex");
        int texCoordAttribLocation = GLES20.glGetAttribLocation(_renderVideoShader, "texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(vertexAttribLocation);
        GLES20.glEnableVertexAttribArray(texCoordAttribLocation);

        // Prepare the quad coordinate data
        GLES20.glVertexAttribPointer(vertexAttribLocation, 2,
                GLES20.GL_FLOAT, false,
                0, _vertexBuffer);

        GLES20.glVertexAttribPointer(texCoordAttribLocation, 2,
                GLES20.GL_FLOAT, false,
                0, _uvBuffer);

        // Draw the quad
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, _indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertexAttribLocation);
        GLES20.glDisableVertexAttribArray(texCoordAttribLocation);
        GLES20.glUseProgram(0);
    }

    private int uploadBitmap(Bitmap bitmap){
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, tex[0]);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR);

        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);

        // upload the bitmap, no mipmap are created
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        return tex[0];
    }

    // upload the data into buffer
    private FloatBuffer createBuffer(float[] data) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);

        return buffer;
    }

    private ShortBuffer createBuffer(short[] data) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(data.length * 2);
        byteBuf.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = byteBuf.asShortBuffer();
        buffer.put(data);
        buffer.position(0);

        return buffer;
    }

    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);	//compile[0] != 0 : compiled successfully
        if (compiled[0] == 0) {
            System.out.println("Error compiling shader:");
            System.out.println(shaderCode);
            System.out.println("----> Error:");
            System.out.println(GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private static int createShaderProgram(String vShaderSrc, String pShaderSrc){
        int shader = GLES20.glCreateProgram();
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_VERTEX_SHADER, vShaderSrc));   // add the vertex shader to program
        GLES20.glAttachShader(shader, loadShader(GLES20.GL_FRAGMENT_SHADER, pShaderSrc)); // add the fragment shader to program
        GLES20.glLinkProgram(shader);                                                     // creates OpenGL ES program executables

        return shader;
    }

    private Queue<Bitmap> _bitmaps = new ArrayDeque<Bitmap>();
    private List<int[]> _glTextures = new ArrayList<int[]>();
    private boolean _needClear = false;

    private int _windowSize = 1;

    private FloatBuffer _vertexBuffer = null;
    private FloatBuffer _uvBuffer = null;
    private ShortBuffer _indexBuffer = null;
    private int _renderVideoShader = 0;

    private static final String vertexShader_src =
            "attribute vec2 vertex; \n" +
            "attribute vec2 texCoord; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "   v_texCoord = texCoord; \n" +
            "   gl_Position = vec4(vertex,0,1); \n" +
            "}";

    private static final String pixelShader_src =
            "precision mediump float; \n" +
            "uniform sampler2D texture[1]; \n" +
            "varying vec2 v_texCoord; \n" +
            "void main(){ \n" +
            "   gl_FragColor = texture2D( texture[0], v_texCoord ); \n" +
            "}";
}