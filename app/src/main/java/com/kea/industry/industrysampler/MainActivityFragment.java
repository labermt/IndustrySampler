package com.kea.industry.industrysampler;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kea.industry.stacklight.Segment;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private RadioButton radioButtonBlink_ = null;
    private RadioButton radioButtonOn_ = null;
    private RadioButton radioButtonOff_ = null;
    private Segment stackLightSegmentCurrent_ = null;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity fragmentActivity = getActivity();

        final RadioGroup radioGroup = (RadioGroup) fragmentActivity.findViewById(R.id.radioGroupSegmentState);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
            }
        });

        radioButtonBlink_ = (RadioButton) fragmentActivity.findViewById(R.id.radioButtonBlink);
        radioButtonOn_ = (RadioButton) fragmentActivity.findViewById(R.id.radioButtonOn);
        radioButtonOff_ = (RadioButton) fragmentActivity.findViewById(R.id.radioButtonOff);

        radioButtonBlink_.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (stackLightSegmentCurrent_ != null) {
                    stackLightSegmentCurrent_.setBlink(true);
                }
            }
        });

        radioButtonOn_.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (stackLightSegmentCurrent_ != null) {
                    stackLightSegmentCurrent_.setOnOff(true);
                }
            }
        });

        radioButtonOff_.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (stackLightSegmentCurrent_ != null) {
                    stackLightSegmentCurrent_.setOnOff(false);
                }
            }
        });

        final View.OnClickListener stackLightSegmentListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stackLightSegmentCurrent_ = (Segment) v;
                final boolean off = stackLightSegmentCurrent_.getOff();
                final boolean on = stackLightSegmentCurrent_.getOn();
                final boolean blink = stackLightSegmentCurrent_.getBlink();
                if (blink) {
                    radioButtonBlink_.setChecked(true);
                } else if (on) {
                    radioButtonOn_.setChecked(true);
                } else {
                    radioButtonOff_.setChecked(true);
                }
            }
        };

        final Segment stackLightSegmentRed = (Segment) fragmentActivity.findViewById(R.id.stack_light_segment_red);
        final Segment stackLightSegmentAmber = (Segment) fragmentActivity.findViewById(R.id.stack_light_segment_amber);
        final Segment stackLightSegmentGreen = (Segment) fragmentActivity.findViewById(R.id.stack_light_segment_green);
        final Segment stackLightSegmentBlue = (Segment) fragmentActivity.findViewById(R.id.stack_light_segment_blue);
        final Segment stackLightSegmentWhite = (Segment) fragmentActivity.findViewById(R.id.stack_light_segment_white);

        stackLightSegmentRed.setOnClickListener(stackLightSegmentListener);
        stackLightSegmentAmber.setOnClickListener(stackLightSegmentListener);
        stackLightSegmentGreen.setOnClickListener(stackLightSegmentListener);
        stackLightSegmentBlue.setOnClickListener(stackLightSegmentListener);
        stackLightSegmentWhite.setOnClickListener(stackLightSegmentListener);
/*
        final FragmentActivity fragmentActivity = getActivity();
        final StackLight stackLight = (StackLight) fragmentActivity.findViewById(R.id.stack_light);
        final StackLightSegment stackLightSegment = new StackLightSegment(fragmentActivity);
        stackLightSegment.setColorResId(R.color.teal);
        stackLightSegment.setBlink(true);
        stackLightSegment.setBlinkDuration(200);
        stackLight.addView(stackLightSegment);

        final StackLightSegment stackLightSegmentRed = (StackLightSegment) getActivity().findViewById(R.id.stack_light_segment_red);
        stackLightSegmentRed.setBlinkDuration(2000);
*/
    }
}
