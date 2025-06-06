
import React, { useEffect, useState } from 'react';

const Tag = ({ tag , specialCssText, specialCssImage }) => {
    const [tagDisplay, setTagDisplay] = React.useState(tag);
    const [tagColor, setTagColor] = React.useState('bg-primary/20');

    useEffect(() => {
        switch (tagDisplay) {
            case 'event':
                setTagDisplay('Dogodek');
                setTagColor('bg-blue-200');
                break;
            case 'sport':
                setTagDisplay('Šport');
                setTagColor('bg-red-200');
                break;
            case 'school':
                setTagDisplay('Šola');
                setTagColor('bg-yellow-200');
                break;
            case 'other':
                setTagDisplay('Drugo');
                setTagColor('bg-purple-200');
                break;
            default:
                setTagDisplay(tag);
        }
    }, [tag]);

    return (
        <div className={`flex items-center h-full w-full rounded font-semibold ${tagColor} ${specialCssText ? specialCssText : ''}`}>
            <img
                src="../icons/hashtag.svg"
                className={`${specialCssImage ? specialCssImage : ''}`}
                alt="hashtag"
            />
            {tagDisplay}
        </div>
    );
}

export default Tag;