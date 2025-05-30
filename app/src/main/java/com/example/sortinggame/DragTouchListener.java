package com.example.sortinggame;

import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

public class DragTouchListener implements View.OnDragListener {
    private GarbageItem.Type expectedType;
    private GameActivity gameActivity;

    public DragTouchListener(GarbageItem.Type expectedType, GameActivity gameActivity) {
        this.expectedType = expectedType;
        this.gameActivity = gameActivity;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Only accept drags that contain garbage items
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Visual feedback when item enters drop zone
                v.setAlpha(0.7f);
                v.setScaleX(1.1f);
                v.setScaleY(1.1f);
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                // Reset visual feedback
                v.setAlpha(1.0f);
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
                return true;

            case DragEvent.ACTION_DROP:
                // Reset visual feedback
                v.setAlpha(1.0f);
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);

                // Get the dragged item
                View draggedView = (View) event.getLocalState();
                if (draggedView == null) {
                    return false;
                }

                Object tagObject = draggedView.getTag();
                if (!(tagObject instanceof GarbageItem)) {
                    return false;
                }

                GarbageItem garbageItem = (GarbageItem) tagObject;

                // Reset dragging state
                draggedView.setTag(R.id.dragging, false);

                // Check if dropped in correct bin
                if (garbageItem.getType() == expectedType) {
                    // Correct sort - remove item and update score
                    gameActivity.removeGarbageItem(draggedView);
                    gameActivity.updateScore(10);
                    return true;
                } else {
                    // Wrong sort - lose a life but still remove the item
                    gameActivity.loseLife();
                    gameActivity.removeGarbageItem(draggedView);
                    return true; // Return true to indicate we handled the drop
                }

            case DragEvent.ACTION_DRAG_ENDED:
                // Reset visual feedback
                v.setAlpha(1.0f);
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);

                // If the drag was not successful and the item still exists
                View draggedView2 = (View) event.getLocalState();
                if (draggedView2 != null && !event.getResult()) {
                    // Reset dragging state
                    draggedView2.setTag(R.id.dragging, false);
                    // Make sure the item is visible
                    draggedView2.setVisibility(View.VISIBLE);
                    draggedView2.setAlpha(1.0f);
                }
                return true;

            default:
                return false;
        }
    }
}
