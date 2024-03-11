package com.example.greetingcard.training.classification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.example.greetingcard.ui.HomeViewModel;
import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PoseClassifierProcessor {
  private static final String TAG = "PoseClassifierProcessor";
  //private static final String POSE_SAMPLES_FILE = "pose/fitness_pose_samples.csv";
  private static final String POSE_SAMPLES_FILE = "pose/exercise_data.csv";

  private static final String PUSHUPS_CLASS = "pushups_down";
  private static final String SQUATS_CLASS = "squats_down";
  private static final String DOWNDOG_CLASS = "downdog";
  private static final String WARRIOR_CLASS = "warrior";
  private static final String TREE_CLASS = "tree";
  private static final String JUMPING_JACKS_CLASS = "jumping_jacks_down";
  private static final String PULLUPS_CLASS = "pullups_down";
  private static final String SITUP_CLASS = "situp_down";
  private static final String[] POSE_CLASSES = {
    //PUSHUPS_CLASS, SQUATS_CLASS
      //DOWNDOG_CLASS, WARRIOR_CLASS, TREE_CLASS
          PUSHUPS_CLASS, SQUATS_CLASS, JUMPING_JACKS_CLASS, PULLUPS_CLASS, SITUP_CLASS
  };

  private final boolean isStreamMode;

  private EMASmoothing emaSmoothing;
  private List<RepetitionCounter> repCounters;
  private HomeViewModel homeViewModel;
  private PoseClassifier poseClassifier;
  private String lastRepResult;

  @WorkerThread
  public PoseClassifierProcessor(Context context, boolean isStreamMode, HomeViewModel homeViewModel) {
    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    this.isStreamMode = isStreamMode;
    this.homeViewModel = homeViewModel;
    if (isStreamMode) {
      emaSmoothing = new EMASmoothing();
      repCounters = new ArrayList<>();
      lastRepResult = "";
    }
    loadPoseSamples(context);
  }

  private void loadPoseSamples(Context context) {
    List<PoseSample> poseSamples = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(context.getAssets().open(POSE_SAMPLES_FILE)));
      String csvLine = reader.readLine();
      while (csvLine != null) {
        PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
        if (poseSample != null) {
          poseSamples.add(poseSample);
        }
        csvLine = reader.readLine();
      }
    } catch (IOException e) {
      Log.e(TAG, "Error when loading pose samples" + e);
    }
    poseClassifier = new PoseClassifier(poseSamples);
    if (isStreamMode) {
      for (String className : POSE_CLASSES) {
        //repCounters.add(new RepetitionCounter(className));
        homeViewModel.getRepCounters().add(new RepetitionCounter(className));
      }
    }
  }

  @WorkerThread
  public List<String> getPoseResult(Pose pose) {
    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    List<String> result = new ArrayList<>();
    ClassificationResult classification = poseClassifier.classify(pose);

    if (isStreamMode) {

      classification = emaSmoothing.getSmoothedResult(classification);

      if (pose.getAllPoseLandmarks().isEmpty()) {
        result.add(lastRepResult);
        return result;
      }

      //for (RepetitionCounter repCounter : repCounters) {
      for (RepetitionCounter repCounter : homeViewModel.getRepCounters()) {
        int repsBefore = repCounter.getNumRepeats();
        int repsAfter = repCounter.addClassificationResult(classification);
        if (repsAfter > repsBefore) {
          ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
          tg.startTone(ToneGenerator.TONE_PROP_BEEP);
          lastRepResult = String.format(
              Locale.US, "%s : %d reps", repCounter.getClassName(), repsAfter);
          break;
        }
      }
      result.add(lastRepResult);
    }

    // Add maxConfidence class of current frame to result if pose is found.
    if (!pose.getAllPoseLandmarks().isEmpty()) {
      String maxConfidenceClass = classification.getMaxConfidenceClass();
      String maxConfidenceClassResult = String.format(
          Locale.US,
          "%s : %.2f confidence",
          maxConfidenceClass,
          classification.getClassConfidence(maxConfidenceClass)
              / poseClassifier.confidenceRange());
      result.add(maxConfidenceClassResult);
    }

    return result;
  }

}
