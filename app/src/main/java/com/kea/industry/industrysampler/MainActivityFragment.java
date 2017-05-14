package com.kea.industry.industrysampler;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

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
