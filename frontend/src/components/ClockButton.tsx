"use client";

interface ClockButtonProps {
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  variant?: "primary" | "secondary" | "accent" | "danger";
  disabled?: boolean;
  time?: string;
}

const variantStyles = {
  primary:
    "bg-gradient-to-br from-primary to-primary-dark text-white shadow-pop hover:shadow-pop-lg",
  secondary:
    "bg-gradient-to-br from-secondary to-cyan-600 text-white shadow-glow",
  accent:
    "bg-gradient-to-br from-accent to-pink-600 text-white",
  danger:
    "bg-gradient-to-br from-danger to-red-600 text-white",
};

export default function ClockButton({
  label,
  icon,
  onClick,
  variant = "primary",
  disabled = false,
  time,
}: ClockButtonProps) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`
        relative flex flex-col items-center justify-center
        w-full min-h-[120px] rounded-3xl p-4
        font-bold text-lg
        hover:scale-105 active:scale-95
        transition-all duration-200
        disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:scale-100
        ${variantStyles[variant]}
      `}
    >
      <span className="text-3xl mb-2">{icon}</span>
      <span>{label}</span>
      {time && (
        <span className="text-sm opacity-80 mt-1">{time}</span>
      )}
    </button>
  );
}
