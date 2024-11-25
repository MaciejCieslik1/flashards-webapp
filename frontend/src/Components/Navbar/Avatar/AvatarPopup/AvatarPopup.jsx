import React from "react";

import './AvatarPopup.css';

const AvatarPopup = (props) => {
    return <div className="avatar-popup">
        <div className="avatar-popup-info">
            <img src={props.avatar} alt="User Avatar" />
            <div>{props.email}</div>
        </div>
        <ul>
            <li>Settings</li>
            <li>Light Mode</li>
            <li>Logout</li>
        </ul>
    </div>
}

export default AvatarPopup;