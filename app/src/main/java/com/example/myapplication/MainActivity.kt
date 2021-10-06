package com.example.myapplication

import android.app.AlertDialog
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var arFragment: ArFragment
    private lateinit var model: Uri
    private var renderable: ModelRenderable?=null
    private var animator: ModelAnimator?=null
    private lateinit var record: Button
    private lateinit var videoRecorder: VideoRecorder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment=sceneform_fragment as ArFragment
        model=Uri.parse("model_fight.sfb")
        record = findViewById(R.id.record);
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            if(plane.type!= Plane.Type.HORIZONTAL_UPWARD_FACING)
            {
                return@setOnTapArPlaneListener
            }
            var anchor = hitResult.createAnchor();

            placeObject(arFragment,anchor,model)

        }
        animate_kick_button.setOnClickListener { animateModel("Character|Kick") }
        animate_idle_button.setOnClickListener { animateModel("Character|Idle") }
        animate_boxing_button.setOnClickListener { animateModel("Character|Boxing") }

        videoRecorder = VideoRecorder()

        videoRecorder.setSceneView(arFragment.arSceneView)

        val orientation = resources.configuration.orientation
        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)


        record.setOnClickListener {
            if(record.text.equals("RECORD")){
                record.setText("STOP")
                val recording = videoRecorder.onToggleRecord()
                Toast.makeText(this,"Recording Started",Toast.LENGTH_SHORT).show();
            }
            else
            {
                record.setText("RECORD")
                val recording = videoRecorder.onToggleRecord()
                Toast.makeText(this,"Recording saved to your gallery",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun animateModel(name: String) {
        animator?.let {
            if(it.isRunning)
            {
                it.end()
            }
        }
        renderable?.let { modelRenderable ->

            val data = modelRenderable.getAnimationData(name)
            animator = ModelAnimator(data,modelRenderable)
            animator?.start()
        }

    }

    private fun placeObject(arFragment: ArFragment, anchor: Anchor?, model: Uri?) {
//for placing
        ModelRenderable.builder()
            .setSource(arFragment.context,model)
            .build()
            .thenAccept {
                renderable = it

                addToScene(arFragment,anchor,it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null

            }

    }

    private fun addToScene(arFragment: ArFragment, anchor: Anchor?, it: ModelRenderable?) {

        val anchorNode = AnchorNode(anchor)
        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable
        val node = TransformableNode(arFragment.transformationSystem)
        node.addChild(skeletonNode)
        node.setParent(anchorNode)

        arFragment.arSceneView.scene.addChild(anchorNode)

    }
}
